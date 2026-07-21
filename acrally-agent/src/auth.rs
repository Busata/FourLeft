//! Process-wide agent credential state.
//!
//! The API key is read here per request instead of being cached in each HTTP
//! client, so a re-pair from the running UI takes effect everywhere at once.
//! A 401 from any endpoint flips one shared "revoked" flag: senders stop
//! hammering a key that can only keep failing, the UI shows a re-pair banner
//! instead of a misleading "not connected", and pairing a new key clears the
//! flag and resumes normal reporting without a restart.

use std::sync::atomic::{AtomicBool, Ordering};
use std::sync::Mutex;

use crate::logfile::agent_log;

/// User-facing explanation + fix, shared by the UI and CLI surfaces.
pub const REVOKED_MSG: &str = "not authorized — this agent's key was revoked. \
    Re-pair to continue (Re-pair in the app, or `acrally-agent pair`).";

static KEY: Mutex<Option<String>> = Mutex::new(None);
static REVOKED: AtomicBool = AtomicBool::new(false);

/// Seed the key from the loaded config. Called once at startup.
pub fn init(key: Option<String>) {
    if let Ok(mut k) = KEY.lock() {
        *k = key;
    }
}

/// Adopt a freshly paired key and clear the revoked flag — every client picks
/// it up on its next request. Only the UI re-pairs in-process; the CLI `pair`
/// flow writes the config and exits.
#[cfg_attr(not(feature = "ui"), allow(dead_code))]
pub fn set_key(key: String) {
    if let Ok(mut k) = KEY.lock() {
        *k = Some(key);
    }
    if REVOKED.swap(false, Ordering::Relaxed) {
        agent_log!("new agent key adopted — resuming normal reporting");
    }
}

/// The current key, if any.
pub fn key() -> Option<String> {
    KEY.lock().ok().and_then(|k| k.clone())
}

/// Record that the backend rejected our key (HTTP 401). Logged once per
/// transition so per-second heartbeats can't flood the log.
pub fn on_unauthorized(context: &str) {
    if !REVOKED.swap(true, Ordering::Relaxed) {
        agent_log!(
            "backend rejected the agent's key (401 on {context}) — the key was revoked or \
             replaced; holding off sends until the agent is re-paired"
        );
    }
}

/// Whether the backend has rejected our key and no re-pair has happened since.
pub fn revoked() -> bool {
    REVOKED.load(Ordering::Relaxed)
}
