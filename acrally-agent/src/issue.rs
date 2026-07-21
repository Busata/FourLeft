//! "Report issue" client: sends a user-written description to the backend together
//! with the current save game and agent log, so a problem can be debugged from the
//! actual artifacts instead of a prose reconstruction.
//!
//!   POST {api_base}/agent/issues   { description, agent_version,
//!                                    save_game_b64, save_game_name, log_b64, log_name }
//!
//! Authenticated by the paired API key, like the ingestion endpoints. The files are
//! base64-encoded into the JSON body (no multipart client in ureq); both are small —
//! the save is ~1 MB and the log rotates at 1 MB — but a longer timeout than the
//! telemetry calls' 10 s is used because this is a real upload.
//!
//! Missing files are not an error: a report is still useful when the save can't be
//! located (that may be the very problem being reported), so whatever exists is
//! attached and the rest is omitted.

use std::time::Duration;

use anyhow::{anyhow, bail, Context, Result};

use crate::config::Config;
use crate::logfile::{self, agent_log};

/// Upper bound per attachment; the backend enforces the same limit. A save or log
/// beyond this is truncated to its **tail** (the newest, most relevant part).
const MAX_ATTACHMENT_BYTES: usize = 16 * 1024 * 1024;
/// Generous upload timeout (the telemetry calls use 10 s).
const TIMEOUT: Duration = Duration::from_secs(60);

/// What a submitted report ended up containing, for honest user feedback: which
/// attachments were actually found and sent (a missing save may be the very bug).
pub struct Receipt {
    pub id: String,
    pub save_attached: bool,
    pub log_attached: bool,
}

/// Submit an issue report. Returns a receipt with the server-assigned report id.
pub fn submit(cfg: &Config, description: &str) -> Result<Receipt> {
    if description.trim().is_empty() {
        bail!("please describe the issue first.");
    }
    if crate::auth::key().is_none() {
        bail!("not paired — connect the agent to your account first.");
    }

    let save = read_save(cfg);
    let log = read_log();
    agent_log!(
        "submitting issue report (save: {}, log: {})",
        save.as_ref().map_or("missing".to_string(), |(_, b)| format!("{} bytes", b.len())),
        log.as_ref().map_or("missing".to_string(), |(_, b)| format!("{} bytes", b.len())),
    );

    let body = serde_json::json!({
        "description": description,
        "agent_version": env!("CARGO_PKG_VERSION"),
        "save_game_b64": save.as_ref().map(|(_, bytes)| b64(bytes)),
        "save_game_name": save.as_ref().map(|(name, _)| name),
        "log_b64": log.as_ref().map(|(_, bytes)| b64(bytes)),
        "log_name": log.as_ref().map(|(name, _)| name),
    });

    let agent = ureq::AgentBuilder::new().timeout(TIMEOUT).build();
    let url = format!(
        "{}/agent/issues",
        cfg.api_base.trim_end_matches('/')
    );
    let mut req = agent.post(&url);
    if let Some(key) = crate::auth::key() {
        req = req.set("Authorization", &format!("Bearer {key}"));
    }
    let response = req.send_json(&body).map_err(submit_error)?;
    let id = response
        .into_json::<serde_json::Value>()
        .ok()
        .and_then(|v| v.get("issue_id").and_then(|x| x.as_str()).map(String::from))
        .unwrap_or_default();
    agent_log!("issue report submitted ({})", if id.is_empty() { "no id" } else { &id });
    Ok(Receipt {
        id,
        save_attached: save.is_some(),
        log_attached: log.is_some(),
    })
}

/// Turn a ureq error into a friendly message, surfacing the backend's reason on a 4xx
/// (empty description, rate limit, attachment too large).
fn submit_error(e: ureq::Error) -> anyhow::Error {
    match e {
        ureq::Error::Status(401, _) => {
            crate::auth::on_unauthorized("issue report");
            anyhow!(crate::auth::REVOKED_MSG)
        }
        ureq::Error::Status(status, resp) => {
            let body = resp.into_string().unwrap_or_default();
            let reason = extract_reason(&body);
            if reason.is_empty() {
                anyhow!("the backend refused the report (HTTP {status})")
            } else {
                anyhow!(reason)
            }
        }
        other => anyhow!("could not reach the club backend ({other})"),
    }
}

/// Pull the human-readable reason out of a Spring error body (`{"message": "..."}`),
/// falling back to the raw text.
fn extract_reason(body: &str) -> String {
    serde_json::from_str::<serde_json::Value>(body)
        .ok()
        .and_then(|v| v.get("message").and_then(|m| m.as_str()).map(String::from))
        .unwrap_or_else(|| body.trim().to_string())
}

/// The save game, if it can be located and read: (file name, bytes).
fn read_save(cfg: &Config) -> Option<(String, Vec<u8>)> {
    let path = crate::savegame::locate_save(cfg.save_path.as_deref())?;
    let bytes = std::fs::read(&path).ok()?;
    let name = path
        .file_name()
        .map(|n| n.to_string_lossy().into_owned())
        .unwrap_or_else(|| "PlayerDataSaveSlot.sav".to_string());
    Some((name, tail(bytes)))
}

/// The agent log, if present: (file name, bytes). Read before the report is sent, so
/// it includes the pipeline events leading up to the problem.
fn read_log() -> Option<(String, Vec<u8>)> {
    let bytes = std::fs::read(logfile::path()).ok()?;
    Some(("agent.log".to_string(), tail(bytes)))
}

/// Keep at most the newest MAX_ATTACHMENT_BYTES of a file.
fn tail(bytes: Vec<u8>) -> Vec<u8> {
    if bytes.len() > MAX_ATTACHMENT_BYTES {
        bytes[bytes.len() - MAX_ATTACHMENT_BYTES..].to_vec()
    } else {
        bytes
    }
}

/// Standard base64 with padding. Hand-rolled (like the log's timestamp formatting)
/// to keep the agent dependency-light.
fn b64(data: &[u8]) -> String {
    const TABLE: &[u8; 64] = b"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    let mut out = String::with_capacity(data.len().div_ceil(3) * 4);
    for chunk in data.chunks(3) {
        let n = (u32::from(chunk[0]) << 16)
            | (u32::from(chunk.get(1).copied().unwrap_or(0)) << 8)
            | u32::from(chunk.get(2).copied().unwrap_or(0));
        out.push(TABLE[(n >> 18) as usize & 63] as char);
        out.push(TABLE[(n >> 12) as usize & 63] as char);
        out.push(if chunk.len() > 1 { TABLE[(n >> 6) as usize & 63] as char } else { '=' });
        out.push(if chunk.len() > 2 { TABLE[n as usize & 63] as char } else { '=' });
    }
    out
}

/// `acrally-agent report-issue <description...>`: console counterpart of the UI's
/// Report issue button, for headless agents (e.g. under Wine).
pub fn run_report(cfg: &Config, description: &str) -> Result<()> {
    println!("collecting the save game and agent log…");
    let receipt = submit(cfg, description).context("could not submit the issue report")?;
    if receipt.id.is_empty() {
        println!("report submitted — thanks!");
    } else {
        println!("report {} submitted — thanks!", receipt.id);
    }
    println!("attached: {}", attachments_line(&receipt));
    Ok(())
}

/// "save game + agent log", "agent log only (no save game found)", …
pub fn attachments_line(receipt: &Receipt) -> String {
    match (receipt.save_attached, receipt.log_attached) {
        (true, true) => "save game + agent log".to_string(),
        (true, false) => "save game only (no agent log found)".to_string(),
        (false, true) => "agent log only (no save game found)".to_string(),
        (false, false) => "nothing — neither the save game nor the log was found".to_string(),
    }
}

// --- UI front-end ----------------------------------------------------------------

/// Send state driven by a background thread and rendered by the Report-issue modal.
#[cfg(feature = "ui")]
#[derive(Clone, Default)]
pub enum SendState {
    #[default]
    Idle,
    Sending,
    /// Sent; the string is the attachments summary (see [`attachments_line`]).
    Sent(String),
    Failed(String),
}

#[cfg(feature = "ui")]
pub type SendHandle = std::sync::Arc<std::sync::Mutex<SendState>>;

/// Submit in the background; the modal polls `handle` for the outcome.
#[cfg(feature = "ui")]
pub fn send(cfg: Config, handle: SendHandle, description: String) {
    if let Ok(mut s) = handle.lock() {
        *s = SendState::Sending;
    }
    std::thread::spawn(move || {
        let outcome = match submit(&cfg, &description) {
            Ok(receipt) => SendState::Sent(attachments_line(&receipt)),
            Err(e) => SendState::Failed(e.to_string()),
        };
        if let Ok(mut s) = handle.lock() {
            *s = outcome;
        }
    });
}

#[cfg(test)]
mod tests {
    use super::b64;

    #[test]
    fn base64_matches_reference_vectors() {
        // RFC 4648 test vectors.
        assert_eq!(b64(b""), "");
        assert_eq!(b64(b"f"), "Zg==");
        assert_eq!(b64(b"fo"), "Zm8=");
        assert_eq!(b64(b"foo"), "Zm9v");
        assert_eq!(b64(b"foob"), "Zm9vYg==");
        assert_eq!(b64(b"fooba"), "Zm9vYmE=");
        assert_eq!(b64(b"foobar"), "Zm9vYmFy");
    }
}
