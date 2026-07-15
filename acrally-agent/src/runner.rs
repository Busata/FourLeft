//! Orchestrates the hybrid flow:
//!   - shared memory drives the live session (start on movement, stream heartbeats);
//!   - an always-on save-file watcher supplies the authoritative penalised results.
//!
//! The two are deliberately decoupled. The live-session heuristics (start/restart/
//! finish inferred from a glitchy timer string) only affect live status, heartbeats
//! and which session a result is attached to — a wrong guess there costs cosmetics,
//! never data. Results come solely from the watcher: any record that appears in the
//! save while the agent runs is posted, attached to the most plausible session. A
//! missed start or a mistimed finish can no longer lose a result, and a finished
//! run's record is never abandoned because a new run began.
//!
//! Result resolution is content-based, never mtime-based: a result is a newest
//! record whose identity — timestamp *plus* times, see [`StageRecord::content_key`]
//! — differs from the floor. The timestamp alone is not enough: the game keeps one
//! record slot per event entry, stamped at event entry and overwritten by each
//! completed run in that event, so a second run of the same event changes the times
//! but not the timestamp. A restart writes no record, so nothing fires; the save
//! also carries non-record data (progress/career), so its mtime moves at times that
//! have nothing to do with a result landing — an mtime gate would misfire on those.
//!
//! The floor of the last posted record is persisted (`last_result` in the config
//! dir) so records that land while the agent is closed are posted on the next
//! launch, each attached to a fresh recovery session.

use std::collections::VecDeque;
use std::path::PathBuf;
use std::time::{Instant, SystemTime, UNIX_EPOCH};

use crate::config::Config;
use crate::logfile::agent_log;
use crate::model::{fmt_ms, parse_laptime_ms, Frame, Heartbeat, ResultPayload, SessionStart};
use crate::savegame;
use crate::savegame::{RecordKey, StageRecord};
use crate::session::{SessionEvent, SessionMachine};
use crate::status::{AgentStatus, StatusHandle};
use crate::submit::Client;

struct Active {
    id: String,
    driver: String,
    /// A record was already posted to this session while it was live (finish
    /// detection lagged the save write) — don't wait for another on finish.
    resolved: bool,
}

/// A finished live session waiting for its save record — the preferred session to
/// attach the next new record to. Held until a record arrives or the wait window
/// lapses; lapsing aborts it server-side ("no-result") purely for tidiness — the
/// watcher keeps running, so even a later record is still posted, attached to the
/// best remaining session.
struct AwaitingResult {
    id: String,
    driver: String,
    deadline: Instant,
}

/// Content-based novelty gate over the save file: tracks the identity of the
/// record that already existed at startup / was last posted (`floor`), and
/// requires a new record to be sighted twice before it's trusted — a read racing
/// the game's save write can see a half-written file. Keyed on the full
/// [`RecordKey`], not just the timestamp: a second run of the same event
/// overwrites the slot's times under an unchanged timestamp and must still post.
struct SaveWatcher {
    floor: Option<RecordKey>,
    candidate: Option<RecordKey>,
}

#[derive(Debug, PartialEq)]
enum WatchDecision {
    Nothing,
    /// A new record was sighted once; re-read soon to confirm it.
    Confirming(RecordKey),
    /// The sighting was reproduced — post it.
    Post(StageRecord),
}

impl SaveWatcher {
    fn new(floor: Option<RecordKey>) -> Self {
        SaveWatcher {
            floor,
            candidate: None,
        }
    }

    fn floor_ticks(&self) -> i64 {
        self.floor.map_or(0, |(ticks, _, _)| ticks)
    }

    fn observe(&mut self, newest: Option<StageRecord>) -> WatchDecision {
        let Some(rec) = newest else {
            self.candidate = None;
            return WatchDecision::Nothing;
        };
        let key = rec.content_key();
        // Known content, or an older event entry resurfacing as "newest" (the
        // game's bounded history evicted the floor record): not a new run.
        if Some(key) == self.floor || rec.timestamp_ticks < self.floor_ticks() {
            self.candidate = None;
            return WatchDecision::Nothing;
        }
        if self.candidate == Some(key) {
            // Confirmed. Raise the floor now, before delivery: per the contract
            // results are never spooled, so a failed POST drops the record rather
            // than retrying it forever (a restart retries it via the persisted
            // floor, which only advances on successful delivery).
            self.candidate = None;
            self.floor = Some(key);
            WatchDecision::Post(rec)
        } else {
            self.candidate = Some(key);
            WatchDecision::Confirming(key)
        }
    }
}

/// Where the last successfully posted record's identity is persisted, so runs
/// that finish while the agent is closed are recovered on the next launch. This
/// is a bookmark into the game's own save, not a spooled result (see the no-spool
/// rationale in `submit.rs`): tampering with it can only cause records still in
/// the save to be re-delivered, which the backend de-dupes.
fn floor_path() -> PathBuf {
    crate::config::config_dir().join("last_result")
}

fn load_floor() -> Option<RecordKey> {
    let text = std::fs::read_to_string(floor_path()).ok()?;
    let mut parts = text.split_whitespace();
    let ticks = parts.next()?.parse().ok()?;
    let raw_ms = parts.next()?.parse().ok()?;
    let penalty_ms = parts.next()?.parse().ok()?;
    Some((ticks, raw_ms, penalty_ms))
}

fn store_floor(key: RecordKey) {
    let (ticks, raw_ms, penalty_ms) = key;
    if let Err(e) = std::fs::write(floor_path(), format!("{ticks} {raw_ms} {penalty_ms}\n")) {
        agent_log!("could not persist result floor: {e}");
    }
}

pub struct Runner {
    cfg: Config,
    session: SessionMachine,
    client: Client,
    save_path: Option<PathBuf>,
    active: Option<Active>,
    /// Finished sessions still expecting their record, oldest first.
    awaiting: VecDeque<AwaitingResult>,
    watcher: SaveWatcher,
    next_save_check: Instant,
    /// Most recent session this process opened — the last-resort attach target
    /// when a record appears with nothing active and nothing awaiting.
    last_session: Option<(String, String)>,
    last_heartbeat: Option<Instant>,
    /// Optional live-status sink for the GUI. `None` in headless mode.
    status: Option<StatusHandle>,
}

impl Runner {
    pub fn new(cfg: Config) -> Self {
        Self::with_status(cfg, None)
    }

    /// Like [`Runner::new`] but publishes session/backend state to a shared
    /// [`StatusHandle`] for the UI to read.
    pub fn with_status(cfg: Config, status: Option<StatusHandle>) -> Self {
        let save_path = savegame::locate_save(cfg.save_path.as_deref());
        match &save_path {
            Some(p) => agent_log!("save file: {}", p.display()),
            None => agent_log!(
                "save file not found (set save_path in config) — results can't be read; \
                 live sessions still work"
            ),
        }
        let session = SessionMachine::new(cfg.finish_frames());
        let client = Client::new(&cfg);
        // The watcher's floor starts at the newest record already on disk: within
        // this process, only records that change past it are results. Records that
        // landed while the agent was closed — everything between the persisted
        // floor (last successful post) and that newest record — are recovered
        // explicitly below. Without a persisted floor (first run) history is left
        // alone: there is no way to tell a missed run from an ancient one.
        // A floor stamped in the future is poison, not a bookmark: it came from a
        // false anchor (see savegame::max_plausible_ticks) and would make every
        // genuine record look old, silently dropping all future runs. Reset it to
        // "post everything still in the save" — the backend de-dupes on
        // (user, ticks, total), so re-delivery is harmless and recovers any runs
        // the poisoned floor swallowed.
        let persisted = match load_floor() {
            Some((ticks, _, _)) if ticks > savegame::max_plausible_ticks() => {
                agent_log!(
                    "persisted result floor is stamped in the future (ticks {ticks}) — a false \
                     save anchor poisoned it; discarding and re-delivering the save's records"
                );
                Some((0, 0, 0))
            }
            other => other,
        };
        let newest = newest_key(save_path.as_deref());
        let mut runner = Runner {
            cfg,
            session,
            client,
            save_path,
            active: None,
            awaiting: VecDeque::new(),
            watcher: SaveWatcher::new(newest.or(persisted)),
            next_save_check: Instant::now(),
            last_session: None,
            last_heartbeat: None,
            status,
        };
        if let Some(floor) = persisted {
            runner.recover_missed(floor);
        }
        runner
    }

    /// Post records that landed in the save while the agent was closed: everything
    /// newer than the persisted floor, plus the floor event's slot if a later run
    /// overwrote its times. Oldest first, each via [`Runner::post_record`], which
    /// opens a recovery session when nothing else is live.
    fn recover_missed(&mut self, floor: RecordKey) {
        let Some(path) = self.save_path.as_deref() else {
            return;
        };
        let Ok(bytes) = std::fs::read(path) else {
            return;
        };
        let mut missed: Vec<StageRecord> = savegame::parse_records(&bytes)
            .into_iter()
            .filter(|r| r.timestamp_ticks >= floor.0 && r.content_key() != floor)
            .collect();
        missed.sort_by_key(|r| r.timestamp_ticks);
        for rec in missed {
            agent_log!(
                "recovering record missed while the agent was closed (ticks {}, {} @ {})",
                rec.timestamp_ticks,
                rec.car,
                rec.stage,
            );
            self.post_record(rec);
        }
    }

    /// Apply an update to the shared status, if one is attached.
    fn set_status(&self, update: impl FnOnce(&mut AgentStatus)) {
        if let Some(h) = &self.status {
            if let Ok(mut s) = h.lock() {
                update(&mut s);
            }
        }
    }

    /// Process one telemetry frame.
    pub fn on_frame(&mut self, f: &Frame) {
        self.check_save();

        match self.session.observe(f) {
            Some(SessionEvent::Start) => self.start(f),
            Some(SessionEvent::Progress) => self.heartbeat(f),
            Some(SessionEvent::Restart) => {
                // A restart writes no save record — abort the live session (any
                // finished run still awaiting its record is unaffected), then
                // open a fresh session for the run that just began.
                if let Some(a) = self.active.take() {
                    agent_log!("restart detected — aborting session {}", a.id);
                    self.client.abort_session(&a.id, "restart");
                }
                self.start(f);
            }
            Some(SessionEvent::Finish) => self.finish(),
            None => {}
        }
    }

    fn start(&mut self, f: &Frame) {
        if let Some(a) = self.active.take() {
            // A new run started without a clean finish — abort the stale one.
            agent_log!("session {} superseded by a new run — aborting", a.id);
            self.client.abort_session(&a.id, "superseded");
        }

        let body = SessionStart {
            driver: f.driver.clone(),
            car: f.car.clone(),
            stage: f.track.clone(),
            track: f.track.clone(),
            started_at_ms: now_ms(),
            agent_version: env!("CARGO_PKG_VERSION").to_string(),
        };
        let id = self.client.start_session(&body);
        let connected = !id.starts_with("local-");
        agent_log!("session {id} started — {} on {}", f.car, f.track);
        self.set_status(|s| {
            s.session_id = Some(id.clone());
            s.backend_connected = connected;
            s.sessions_started += 1;
        });
        self.last_session = Some((id.clone(), f.driver.clone()));
        self.active = Some(Active {
            id,
            driver: f.driver.clone(),
            resolved: false,
        });
        self.last_heartbeat = None;
    }

    fn heartbeat(&mut self, f: &Frame) {
        let Some(active) = &self.active else { return };
        let due = self
            .last_heartbeat
            .map_or(true, |t| t.elapsed() >= self.cfg.heartbeat_interval());
        if !due {
            return;
        }
        self.last_heartbeat = Some(Instant::now());
        let hb = Heartbeat {
            current_ms: parse_laptime_ms(&f.current_laptime),
            speed_kmh: f.speed_kmh,
            gear: f.gear,
            rpm: f.rpm,
            distance_m: f.distance_m,
        };
        let ok = self.client.heartbeat(&active.id, &hb);
        self.set_status(|s| s.backend_connected = ok);
    }

    fn finish(&mut self) {
        let Some(a) = self.active.take() else { return };
        if a.resolved {
            agent_log!(
                "finish detected — session {} already has its result, nothing to wait for",
                a.id
            );
            return;
        }
        agent_log!(
            "finish detected — session {} awaiting its save record",
            a.id
        );
        self.awaiting.push_back(AwaitingResult {
            id: a.id,
            driver: a.driver,
            deadline: Instant::now() + self.cfg.save_wait(),
        });
    }

    /// The always-on result path: read the save (throttled), post any record
    /// newer than the floor, and expire finished sessions whose wait lapsed.
    /// Independent of the live-session heuristics by design.
    fn check_save(&mut self) {
        let now = Instant::now();
        if now < self.next_save_check {
            return;
        }
        // Poll fast while something is (or just was) driving; idle slowly the
        // rest of the time — the save only changes when the game writes a run.
        let hot = self.active.is_some() || !self.awaiting.is_empty();
        self.next_save_check = now
            + if hot {
                self.cfg.save_check_interval()
            } else {
                self.cfg.save_idle_check_interval()
            };

        match self.watcher.observe(self.read_newest_record()) {
            WatchDecision::Nothing => {}
            WatchDecision::Confirming((ticks, raw_ms, penalty_ms)) => {
                agent_log!(
                    "new save record sighted (ticks {ticks}, total {}ms) — confirming",
                    raw_ms + penalty_ms
                );
                // Confirm promptly even when otherwise idle.
                self.next_save_check = now + self.cfg.save_check_interval();
            }
            WatchDecision::Post(rec) => self.post_record(rec),
        }

        // Expire after the read above, so a record landing on the deadline still
        // attaches to its own session.
        while let Some(a) = self.awaiting.front() {
            if now < a.deadline {
                break;
            }
            agent_log!(
                "no save record within the wait window for session {} — aborting it \
                 (the watcher keeps running; a late record is still posted)",
                a.id
            );
            self.client.abort_session(&a.id, "no-result");
            self.awaiting.pop_front();
        }
    }

    /// Post a confirmed new record, attached to the most plausible session:
    /// the oldest finished run still waiting for one, else the live session,
    /// else the most recent session this process opened, else a fresh recovery
    /// session — a record is never dropped for lack of a session (run-start
    /// detection is heuristic and does miss runs).
    fn post_record(&mut self, rec: StageRecord) {
        let (id, driver) = if let Some(a) = self.awaiting.pop_front() {
            (a.id, a.driver)
        } else if let Some(a) = &mut self.active {
            a.resolved = true;
            (a.id.clone(), a.driver.clone())
        } else if let Some((id, driver)) = &self.last_session {
            (id.clone(), driver.clone())
        } else {
            let body = SessionStart {
                driver: String::new(),
                car: rec.car.clone(),
                stage: rec.stage.clone(),
                track: rec.stage.clone(),
                started_at_ms: now_ms(),
                agent_version: env!("CARGO_PKG_VERSION").to_string(),
            };
            let id = self.client.start_session(&body);
            agent_log!(
                "no session to attach record (ticks {}) to — opened recovery session {id}",
                rec.timestamp_ticks
            );
            (id, String::new())
        };

        let result = ResultPayload {
            stage: rec.stage,
            car: rec.car,
            driver,
            raw_ms: rec.raw_ms,
            penalty_ms: rec.penalty_ms,
            total_ms: rec.total_ms,
            timestamp_ticks: rec.timestamp_ticks,
            agent_version: env!("CARGO_PKG_VERSION").to_string(),
        };
        let summary = format!(
            "{} @ {} — {} (raw {} + {}s)",
            result.car,
            result.stage,
            fmt_ms(result.total_ms as i32),
            fmt_ms(result.raw_ms as i32),
            result.penalty_ms / 1000,
        );
        let key = (result.timestamp_ticks, result.raw_ms, result.penalty_ms);
        let ok = self.client.post_result(&id, &result);
        if ok {
            // Only a delivered record advances the persisted floor — an
            // undelivered one is retried by recovery on the next launch.
            store_floor(key);
        }
        self.set_status(|s| {
            s.backend_connected = ok;
            if ok {
                s.results_posted += 1;
                s.last_result = Some(summary);
            }
        });
    }

    /// The newest record currently in the save file, if it can be read.
    fn read_newest_record(&self) -> Option<StageRecord> {
        let path = self.save_path.as_ref()?;
        let bytes = std::fs::read(path).ok()?;
        savegame::newest_record(&bytes)
    }
}

/// The newest record's identity in the save at `path` — the in-process startup
/// floor. `None` when the save is missing/unreadable/recordless.
fn newest_key(path: Option<&std::path::Path>) -> Option<RecordKey> {
    path.and_then(|p| std::fs::read(p).ok())
        .and_then(|bytes| savegame::newest_record(&bytes))
        .map(|r| r.content_key())
}

fn now_ms() -> u128 {
    SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .map(|d| d.as_millis())
        .unwrap_or(0)
}

#[cfg(test)]
mod tests {
    use super::*;

    fn timed(ticks: i64, raw_ms: u32) -> StageRecord {
        StageRecord {
            timestamp_ticks: ticks,
            stage: "S".into(),
            car: "C".into(),
            raw_ms,
            penalty_ms: 0,
            total_ms: raw_ms,
        }
    }

    fn rec(ticks: i64) -> StageRecord {
        timed(ticks, 100_000)
    }

    fn key(ticks: i64) -> RecordKey {
        rec(ticks).content_key()
    }

    #[test]
    fn ignores_records_at_or_below_the_floor() {
        let mut w = SaveWatcher::new(Some(key(100)));
        assert_eq!(w.observe(Some(rec(100))), WatchDecision::Nothing);
        assert_eq!(w.observe(Some(rec(50))), WatchDecision::Nothing);
        assert_eq!(w.observe(None), WatchDecision::Nothing);
    }

    #[test]
    fn posts_after_a_confirming_second_sighting() {
        let mut w = SaveWatcher::new(Some(key(100)));
        assert_eq!(w.observe(Some(rec(200))), WatchDecision::Confirming(key(200)));
        assert_eq!(w.observe(Some(rec(200))), WatchDecision::Post(rec(200)));
        // Posted records raise the floor: re-reads go quiet.
        assert_eq!(w.observe(Some(rec(200))), WatchDecision::Nothing);
    }

    #[test]
    fn a_changed_sighting_restarts_confirmation() {
        // A read racing the game's save write can see a half-written record;
        // only two identical consecutive sightings are trusted.
        let mut w = SaveWatcher::new(Some(key(100)));
        assert_eq!(w.observe(Some(rec(200))), WatchDecision::Confirming(key(200)));
        assert_eq!(w.observe(Some(rec(201))), WatchDecision::Confirming(key(201)));
        assert_eq!(w.observe(Some(rec(201))), WatchDecision::Post(rec(201)));
    }

    #[test]
    fn an_unreadable_save_resets_the_candidate() {
        let mut w = SaveWatcher::new(Some(key(100)));
        assert_eq!(w.observe(Some(rec(200))), WatchDecision::Confirming(key(200)));
        assert_eq!(w.observe(None), WatchDecision::Nothing);
        // The sighting must be rebuilt from scratch.
        assert_eq!(w.observe(Some(rec(200))), WatchDecision::Confirming(key(200)));
        assert_eq!(w.observe(Some(rec(200))), WatchDecision::Post(rec(200)));
    }

    #[test]
    fn successive_runs_each_post_once() {
        let mut w = SaveWatcher::new(None);
        assert_eq!(w.observe(Some(rec(10))), WatchDecision::Confirming(key(10)));
        assert_eq!(w.observe(Some(rec(10))), WatchDecision::Post(rec(10)));
        assert_eq!(w.observe(Some(rec(10))), WatchDecision::Nothing);
        assert_eq!(w.observe(Some(rec(20))), WatchDecision::Confirming(key(20)));
        assert_eq!(w.observe(Some(rec(20))), WatchDecision::Post(rec(20)));
    }

    #[test]
    fn a_rerun_of_the_same_event_posts_despite_the_unchanged_timestamp() {
        // The game keeps one slot per event entry and overwrites its times on
        // each completed run — even a slower one (verified live 2026-07-07:
        // 4:52.888 then 4:55.631 under the same ticks). Both must post.
        let mut w = SaveWatcher::new(None);
        assert!(matches!(w.observe(Some(timed(100, 292_888))), WatchDecision::Confirming(_)));
        assert!(matches!(w.observe(Some(timed(100, 292_888))), WatchDecision::Post(_)));
        let slower = timed(100, 295_631);
        assert_eq!(
            w.observe(Some(slower.clone())),
            WatchDecision::Confirming(slower.content_key())
        );
        assert_eq!(w.observe(Some(slower.clone())), WatchDecision::Post(slower));
    }

    #[test]
    fn an_older_record_resurfacing_after_eviction_stays_quiet() {
        // The save keeps a bounded history; if the floor record is evicted the
        // newest can be an older, already-known entry — never re-post it.
        let mut w = SaveWatcher::new(Some(key(200)));
        assert_eq!(w.observe(Some(rec(150))), WatchDecision::Nothing);
        assert_eq!(w.observe(Some(rec(150))), WatchDecision::Nothing);
    }
}
