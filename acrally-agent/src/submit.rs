//! Club-backend client: live sessions + authoritative results.
//!
//! Endpoints (all under `api_base`):
//!   POST /sessions                     -> `{ session_id }`   (driving started)
//!   POST /sessions/{id}/heartbeat      live telemetry        (best-effort)
//!   POST /sessions/{id}/result         penalised result      (retried in-process)
//!   POST /sessions/{id}/abort          `{ reason }`          (best-effort)
//!
//! Heartbeats are ephemeral (dropped on failure). A failed result POST is
//! retried a few times in-process and then dropped — results are deliberately
//! never persisted to disk, so there is no local file a user could edit to
//! forge or replay a result.

use std::time::{Duration, SystemTime, UNIX_EPOCH};

use crate::config::Config;
use crate::model::{Heartbeat, ResultPayload, SessionStart};

/// In-process retries for a failed result POST (with a short pause between).
const RESULT_RETRIES: u32 = 3;
const RESULT_RETRY_PAUSE: Duration = Duration::from_secs(2);

pub struct Client {
    api_base: String,
    api_key: Option<String>,
    agent: ureq::Agent,
}

impl Client {
    pub fn new(cfg: &Config) -> Self {
        Client {
            api_base: cfg.api_base.trim_end_matches('/').to_string(),
            api_key: cfg.api_key.clone(),
            agent: ureq::AgentBuilder::new()
                .timeout(std::time::Duration::from_secs(10))
                .build(),
        }
    }

    fn req(&self, url: &str) -> ureq::Request {
        let mut r = self.agent.post(url);
        if let Some(key) = &self.api_key {
            r = r.set("Authorization", &format!("Bearer {key}"));
        }
        r
    }

    /// Open a session. Returns a session id — from the server if it responds,
    /// otherwise a locally-generated id so heartbeats/results can still be keyed.
    pub fn start_session(&self, body: &SessionStart) -> String {
        let url = format!("{}/sessions", self.api_base);
        match self.req(&url).send_json(body) {
            Ok(resp) => resp
                .into_json::<serde_json::Value>()
                .ok()
                .and_then(|v| {
                    v.get("session_id")
                        .or_else(|| v.get("id"))
                        .and_then(|x| x.as_str())
                        .map(String::from)
                })
                .unwrap_or_else(local_session_id),
            Err(e) => {
                eprintln!("start_session failed ({e}); using local session id");
                local_session_id()
            }
        }
    }

    /// Best-effort live heartbeat (dropped on failure). Returns whether the POST
    /// was accepted, so callers can surface backend connectivity.
    pub fn heartbeat(&self, session_id: &str, body: &Heartbeat) -> bool {
        let url = format!("{}/sessions/{}/heartbeat", self.api_base, session_id);
        self.req(&url).send_json(body).is_ok()
    }

    /// Post the authoritative result, retrying a few times in-process. Returns
    /// `true` if delivered; on total failure the result is dropped (never
    /// persisted for later replay) and `false` is returned.
    pub fn post_result(&self, session_id: &str, result: &ResultPayload) -> bool {
        let url = format!("{}/sessions/{}/result", self.api_base, session_id);
        for attempt in 1..=RESULT_RETRIES {
            match self.req(&url).send_json(result) {
                Ok(_) => {
                    println!(
                        "result: {} @ {} ({}) total {} [raw {} + pen {}]",
                        result.car,
                        result.stage,
                        session_id,
                        fmt(result.total_ms),
                        fmt(result.raw_ms),
                        result.penalty_ms / 1000,
                    );
                    return true;
                }
                Err(e) => {
                    eprintln!("result POST failed (attempt {attempt}/{RESULT_RETRIES}): {e}");
                    if attempt < RESULT_RETRIES {
                        std::thread::sleep(RESULT_RETRY_PAUSE);
                    }
                }
            }
        }
        eprintln!(
            "result for session {session_id} could not be delivered — dropped ({} @ {}, total {})",
            result.car,
            result.stage,
            fmt(result.total_ms),
        );
        false
    }

    /// Best-effort session abort (e.g. on restart / quit).
    pub fn abort_session(&self, session_id: &str, reason: &str) {
        let url = format!("{}/sessions/{}/abort", self.api_base, session_id);
        let _ = self
            .req(&url)
            .send_json(serde_json::json!({ "reason": reason }));
    }
}

fn local_session_id() -> String {
    let n = SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .map(|d| d.as_nanos())
        .unwrap_or(0);
    format!("local-{n}")
}

fn fmt(ms: u32) -> String {
    format!("{}:{:06.3}", ms / 60_000, (ms % 60_000) as f32 / 1000.0)
}
