//! Orchestrates the hybrid flow:
//!   - shared memory drives the live session (start on movement, stream heartbeats);
//!   - the save file supplies the authoritative penalised result on finish.
//!
//! Restarts write no save record, so a restart aborts the session rather than
//! posting a result.

use std::path::PathBuf;
use std::time::{Instant, SystemTime, UNIX_EPOCH};

use crate::config::Config;
use crate::model::{fmt_ms, parse_laptime_ms, Frame, Heartbeat, ResultPayload, SessionStart};
use crate::savegame;
use crate::session::{SessionEvent, SessionMachine};
use crate::status::{AgentStatus, StatusHandle};
use crate::submit::Client;

struct Active {
    id: String,
    driver: String,
    save_mtime_at_start: Option<SystemTime>,
}

struct Pending {
    id: String,
    driver: String,
    save_mtime_at_start: Option<SystemTime>,
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
            save_mtime_at_start: self.save_mtime(),
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
        println!("finish detected — awaiting save file for session {}", a.id);
        self.pending = Some(Pending {
            id: a.id,
            driver: a.driver,
            save_mtime_at_start: a.save_mtime_at_start,
            deadline: Instant::now() + self.cfg.save_wait(),
        });
    }

    /// If a finish is pending, post the result once the save file updates, or
    /// abort if the wait times out (`force` = resolve now, even before timeout).
    fn resolve_pending(&mut self, force: bool) {
        // Snapshot the fields we need so we don't hold a borrow of self.pending
        // while mutating self below.
        let (baseline, deadline, id, driver) = match &self.pending {
            Some(p) => (
                p.save_mtime_at_start,
                p.deadline,
                p.id.clone(),
                p.driver.clone(),
            ),
            None => return,
        };

        if mtime_changed(baseline, self.save_mtime()) {
            if let Some(result) = self.read_result(&driver) {
                self.pending = None;
                if self.last_posted_ticks == Some(result.timestamp_ticks) {
                    return; // already posted this record
                }
                self.last_posted_ticks = Some(result.timestamp_ticks);
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
                return;
            }
        }

        if force || Instant::now() >= deadline {
            self.pending = None;
            eprintln!("no save result for session {id} — aborting");
            self.client.abort_session(&id, "no-result");
        }
    }

    /// Read the newest record from the save file as a result payload.
    fn read_result(&self, driver: &str) -> Option<ResultPayload> {
        let path = self.save_path.as_ref()?;
        let bytes = std::fs::read(path).ok()?;
        let rec = savegame::newest_record(&bytes)?;
        Some(ResultPayload {
            stage: rec.stage,
            car: rec.car,
            driver: driver.to_string(),
            raw_ms: rec.raw_ms,
            penalty_ms: rec.penalty_ms,
            total_ms: rec.total_ms,
            timestamp_ticks: rec.timestamp_ticks,
            agent_version: env!("CARGO_PKG_VERSION").to_string(),
        })
    }

    fn save_mtime(&self) -> Option<SystemTime> {
        let p = self.save_path.as_ref()?;
        std::fs::metadata(p).ok()?.modified().ok()
    }
}

fn mtime_changed(baseline: Option<SystemTime>, current: Option<SystemTime>) -> bool {
    match (baseline, current) {
        (Some(b), Some(c)) => c > b,
        (None, Some(_)) => true,
        _ => false,
    }
}

fn now_ms() -> u128 {
    SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .map(|d| d.as_millis())
        .unwrap_or(0)
}
