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
//! Result resolution is content-based, never mtime-based: only a record with a
//! timestamp newer than the newest one at agent startup (and newer than the last
//! posted) is a result; a restart writes no record, so nothing fires. The save also
//! carries non-record data (progress/career), so its mtime moves at times that have
//! nothing to do with a result landing — an mtime gate would misfire on those.

use std::collections::VecDeque;
use std::path::PathBuf;
use std::time::{Instant, SystemTime, UNIX_EPOCH};

use crate::config::Config;
use crate::logfile::agent_log;
use crate::model::{fmt_ms, parse_laptime_ms, Frame, Heartbeat, ResultPayload, SessionStart};
use crate::savegame;
use crate::savegame::StageRecord;
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

/// Content-based novelty gate over the save file: tracks what already existed at
/// startup / has been posted (`floor_ticks`), and requires a new record to be
/// sighted twice before it's trusted — a read racing the game's save write can
/// see a half-written file.
struct SaveWatcher {
    floor_ticks: i64,
    candidate_ticks: Option<i64>,
}

#[derive(Debug, PartialEq)]
enum WatchDecision {
    Nothing,
    /// A new record was sighted once; re-read soon to confirm it.
    Confirming(i64),
    /// The sighting was reproduced — post it.
    Post(StageRecord),
}

impl SaveWatcher {
    fn new(floor_ticks: i64) -> Self {
        SaveWatcher {
            floor_ticks,
            candidate_ticks: None,
        }
    }

    fn observe(&mut self, newest: Option<StageRecord>) -> WatchDecision {
        let Some(rec) = newest else {
            self.candidate_ticks = None;
            return WatchDecision::Nothing;
        };
        if rec.timestamp_ticks <= self.floor_ticks {
            self.candidate_ticks = None;
            return WatchDecision::Nothing;
        }
        if self.candidate_ticks == Some(rec.timestamp_ticks) {
            // Confirmed. Raise the floor now, before delivery: per the contract
            // results are never spooled, so a failed POST drops the record rather
            // than retrying it forever.
            self.candidate_ticks = None;
            self.floor_ticks = rec.timestamp_ticks;
            WatchDecision::Post(rec)
        } else {
            self.candidate_ticks = Some(rec.timestamp_ticks);
            WatchDecision::Confirming(rec.timestamp_ticks)
        }
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
        // Everything already in the save predates this agent run and must never
        // be posted; only records newer than this floor are results.
        let floor = newest_ticks(save_path.as_deref());
        Runner {
            cfg,
            session,
            client,
            save_path,
            active: None,
            awaiting: VecDeque::new(),
            watcher: SaveWatcher::new(floor),
            next_save_check: Instant::now(),
            last_session: None,
            last_heartbeat: None,
            status,
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
            WatchDecision::Confirming(ticks) => {
                agent_log!("new save record sighted (ticks {ticks}) — confirming");
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
    /// else the most recent session this process opened.
    fn post_record(&mut self, rec: StageRecord) {
        let (id, driver) = if let Some(a) = self.awaiting.pop_front() {
            (a.id, a.driver)
        } else if let Some(a) = &mut self.active {
            a.resolved = true;
            (a.id.clone(), a.driver.clone())
        } else if let Some((id, driver)) = &self.last_session {
            (id.clone(), driver.clone())
        } else {
            agent_log!(
                "save record (ticks {}) appeared but no session was ever opened — not submitting",
                rec.timestamp_ticks
            );
            return;
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
        let ok = self.client.post_result(&id, &result);
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

/// The newest record's timestamp in the save at `path` — the startup floor.
/// 0 when the save is missing/unreadable/recordless.
fn newest_ticks(path: Option<&std::path::Path>) -> i64 {
    path.and_then(|p| std::fs::read(p).ok())
        .and_then(|bytes| savegame::newest_record(&bytes))
        .map(|r| r.timestamp_ticks)
        .unwrap_or(0)
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

    fn rec(ticks: i64) -> StageRecord {
        StageRecord {
            timestamp_ticks: ticks,
            stage: "S".into(),
            car: "C".into(),
            raw_ms: 100_000,
            penalty_ms: 0,
            total_ms: 100_000,
        }
    }

    #[test]
    fn ignores_records_at_or_below_the_floor() {
        let mut w = SaveWatcher::new(100);
        assert_eq!(w.observe(Some(rec(100))), WatchDecision::Nothing);
        assert_eq!(w.observe(Some(rec(50))), WatchDecision::Nothing);
        assert_eq!(w.observe(None), WatchDecision::Nothing);
    }

    #[test]
    fn posts_after_a_confirming_second_sighting() {
        let mut w = SaveWatcher::new(100);
        assert_eq!(w.observe(Some(rec(200))), WatchDecision::Confirming(200));
        assert_eq!(w.observe(Some(rec(200))), WatchDecision::Post(rec(200)));
        // Posted records raise the floor: re-reads go quiet.
        assert_eq!(w.observe(Some(rec(200))), WatchDecision::Nothing);
    }

    #[test]
    fn a_changed_sighting_restarts_confirmation() {
        // A read racing the game's save write can see a half-written record;
        // only two identical consecutive sightings are trusted.
        let mut w = SaveWatcher::new(100);
        assert_eq!(w.observe(Some(rec(200))), WatchDecision::Confirming(200));
        assert_eq!(w.observe(Some(rec(201))), WatchDecision::Confirming(201));
        assert_eq!(w.observe(Some(rec(201))), WatchDecision::Post(rec(201)));
    }

    #[test]
    fn an_unreadable_save_resets_the_candidate() {
        let mut w = SaveWatcher::new(100);
        assert_eq!(w.observe(Some(rec(200))), WatchDecision::Confirming(200));
        assert_eq!(w.observe(None), WatchDecision::Nothing);
        // The sighting must be rebuilt from scratch.
        assert_eq!(w.observe(Some(rec(200))), WatchDecision::Confirming(200));
        assert_eq!(w.observe(Some(rec(200))), WatchDecision::Post(rec(200)));
    }

    #[test]
    fn successive_runs_each_post_once() {
        let mut w = SaveWatcher::new(0);
        assert_eq!(w.observe(Some(rec(10))), WatchDecision::Confirming(10));
        assert_eq!(w.observe(Some(rec(10))), WatchDecision::Post(rec(10)));
        assert_eq!(w.observe(Some(rec(10))), WatchDecision::Nothing);
        assert_eq!(w.observe(Some(rec(20))), WatchDecision::Confirming(20));
        assert_eq!(w.observe(Some(rec(20))), WatchDecision::Post(rec(20)));
    }
}
