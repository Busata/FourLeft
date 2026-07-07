//! Orchestrates the hybrid flow:
//!   - shared memory drives the live session (start on movement, stream heartbeats);
//!   - the save file supplies the authoritative penalised result on finish.
//!
//! Restarts write no save record, so a restart aborts the session rather than
//! posting a result.
//!
//! Result resolution is content-based, never mtime-based: the newest record's
//! timestamp is snapshotted when the run starts, and a finish only resolves when
//! a record NEWER than that snapshot appears. The save also carries non-record
//! data (progress/career), so its mtime moves at times that have nothing to do
//! with a result landing — an mtime gate reads the previous run's record as
//! "the" result and either drops the finish or posts stale data.

use std::path::PathBuf;
use std::time::{Instant, SystemTime, UNIX_EPOCH};

use crate::config::Config;
use crate::model::{fmt_ms, parse_laptime_ms, Frame, Heartbeat, ResultPayload, SessionStart};
use crate::savegame;
use crate::savegame::StageRecord;
use crate::session::{SessionEvent, SessionMachine};
use crate::status::{AgentStatus, StatusHandle};
use crate::submit::Client;

struct Active {
    id: String,
    driver: String,
    /// Newest save-record timestamp when the run began; only a record newer
    /// than this can be this run's result.
    baseline_ticks: i64,
}

struct Pending {
    id: String,
    driver: String,
    baseline_ticks: i64,
    /// A new record sighted on the previous check, awaiting one confirming
    /// re-read — a read that races the game's save write can see a half-written
    /// file, so a single sighting isn't trusted (unless force-resolving).
    candidate_ticks: Option<i64>,
    /// Earliest time of the next save read (throttles full-file parses).
    next_check: Instant,
    deadline: Instant,
}

pub struct Runner {
    cfg: Config,
    session: SessionMachine,
    client: Client,
    save_path: Option<PathBuf>,
    active: Option<Active>,
    pending: Option<Pending>,
    last_heartbeat: Option<Instant>,
    last_posted_ticks: Option<i64>,
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
            Some(p) => println!("save file: {}", p.display()),
            None => eprintln!(
                "save file not found (set save_path in config) — results can't be read; \
                 live sessions still work"
            ),
        }
        let session = SessionMachine::new(cfg.finish_frames());
        let client = Client::new(&cfg);
        Runner {
            cfg,
            session,
            client,
            save_path,
            active: None,
            pending: None,
            last_heartbeat: None,
            last_posted_ticks: None,
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
        // Resolve any pending finish (save updated, or timed out).
        self.resolve_pending(false);

        match self.session.observe(f) {
            Some(SessionEvent::Start) => self.start(f),
            Some(SessionEvent::Progress) => self.heartbeat(f),
            Some(SessionEvent::Restart) => {
                // Abandon whatever is open (a restart writes no save record), then
                // open a fresh session for the run that just began.
                self.resolve_pending(true);
                if let Some(a) = self.active.take() {
                    println!("restart detected — aborting session {}", a.id);
                    self.client.abort_session(&a.id, "restart");
                }
                self.start(f);
            }
            Some(SessionEvent::Finish) => self.finish(),
            None => {}
        }
    }

    fn start(&mut self, f: &Frame) {
        // Finalise anything still pending before opening a new session.
        self.resolve_pending(true);
        if let Some(a) = self.active.take() {
            // A new run started without a clean finish — abort the stale one.
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
        println!("session {id} started — {} on {}", f.car, f.track);
        self.set_status(|s| {
            s.session_id = Some(id.clone());
            s.backend_connected = connected;
            s.sessions_started += 1;
        });
        self.active = Some(Active {
            id,
            driver: f.driver.clone(),
            baseline_ticks: self.newest_save_ticks(),
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
        println!(
            "finish detected — awaiting a save record newer than ticks {} for session {}",
            a.baseline_ticks, a.id
        );
        self.pending = Some(Pending {
            id: a.id,
            driver: a.driver,
            baseline_ticks: a.baseline_ticks,
            candidate_ticks: None,
            next_check: Instant::now(),
            deadline: Instant::now() + self.cfg.save_wait(),
        });
    }

    /// If a finish is pending, post the result once a record newer than the
    /// run-start snapshot appears in the save, or abort if the wait times out
    /// (`force` = resolve now: commit an unconfirmed sighting rather than lose
    /// it, abort if nothing new has landed).
    fn resolve_pending(&mut self, force: bool) {
        // Snapshot the fields we need so we don't hold a borrow of self.pending
        // while mutating self below.
        let (id, driver, baseline, candidate, next_check, deadline) = match &self.pending {
            Some(p) => (
                p.id.clone(),
                p.driver.clone(),
                p.baseline_ticks,
                p.candidate_ticks,
                p.next_check,
                p.deadline,
            ),
            None => return,
        };

        let now = Instant::now();
        if !force && now < next_check {
            return;
        }

        // Only a record newer than the run-start snapshot (and not one we've
        // already delivered) can be this run's result — anything else means the
        // save was written for reasons that aren't a new stage record.
        let fresh = self.read_newest_record().filter(|r| {
            r.timestamp_ticks > baseline && Some(r.timestamp_ticks) != self.last_posted_ticks
        });

        if let Some(rec) = fresh {
            // Trust the record once a re-read reproduces it (a read racing the
            // game's save write can see a half-written file). When forced (a new
            // run is starting), commit the sighting now rather than abort it.
            if force || candidate == Some(rec.timestamp_ticks) {
                self.pending = None;
                self.last_posted_ticks = Some(rec.timestamp_ticks);
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
            } else if let Some(p) = &mut self.pending {
                println!(
                    "new save record sighted (ticks {}) — confirming",
                    rec.timestamp_ticks
                );
                p.candidate_ticks = Some(rec.timestamp_ticks);
                p.next_check = now + self.cfg.save_check_interval();
            }
            return;
        }

        if force || now >= deadline {
            self.pending = None;
            eprintln!("no new save record for session {id} — aborting");
            self.client.abort_session(&id, "no-result");
        } else if let Some(p) = &mut self.pending {
            p.candidate_ticks = None;
            p.next_check = now + self.cfg.save_check_interval();
        }
    }

    /// The newest record currently in the save file, if it can be read.
    fn read_newest_record(&self) -> Option<StageRecord> {
        let path = self.save_path.as_ref()?;
        let bytes = std::fs::read(path).ok()?;
        savegame::newest_record(&bytes)
    }

    /// The newest record's timestamp right now — the baseline a finishing run's
    /// record must beat. 0 when the save is missing/unreadable/recordless.
    fn newest_save_ticks(&self) -> i64 {
        self.read_newest_record()
            .map(|r| r.timestamp_ticks)
            .unwrap_or(0)
    }
}

fn now_ms() -> u128 {
    SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .map(|d| d.as_millis())
        .unwrap_or(0)
}
