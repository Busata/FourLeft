//! Device pairing: link this agent to a user account with no copy-pasting.
//!
//! Implements the client half of the backend's OAuth device-authorization flow
//! (RFC 8628). The agent asks the backend to start a pairing, shows the user a
//! short code + a link to approve it in the browser, then polls until the backend
//! hands back a personal API key — which we write into the config file so the next
//! run just uses it.
//!
//!   POST {api_base}/agent/pair/start  -> { deviceCode, userCode, verificationUri,
//!                                          verificationUriComplete, intervalSeconds,
//!                                          expiresInSeconds }
//!   POST {api_base}/agent/pair/token  { deviceCode } -> { status, apiKey?, label? }
//!         status: pending | approved | denied | expired | consumed
//!
//! Two front-ends use the same primitives (`start` + `poll_once`): the `pair` CLI
//! subcommand (`run`) and the tray UI's Connect screen (`drive`).

use std::time::{Duration, Instant};

use anyhow::{anyhow, Context, Result};
use serde::Deserialize;

use crate::config::Config;

/// Poll cadence floor / pairing lifetime floor, in case the server omits them.
const MIN_INTERVAL_SECS: u64 = 1;
const MIN_EXPIRY_SECS: u64 = 60;

#[derive(Deserialize)]
struct StartResponse {
    #[serde(rename = "deviceCode")]
    device_code: String,
    #[serde(rename = "userCode")]
    user_code: String,
    #[serde(rename = "verificationUri")]
    verification_uri: String,
    #[serde(rename = "verificationUriComplete")]
    verification_uri_complete: String,
    #[serde(rename = "intervalSeconds")]
    interval_seconds: u64,
    #[serde(rename = "expiresInSeconds")]
    expires_in_seconds: u64,
}

#[derive(Deserialize)]
struct TokenResponse {
    status: String,
    #[serde(rename = "apiKey")]
    api_key: Option<String>,
}

/// A started pairing: the device secret we poll with, plus what to show the user.
pub struct Started {
    pub device_code: String,
    pub user_code: String,
    pub verification_uri: String,
    pub verification_uri_complete: String,
    pub interval: Duration,
    pub expires_in: Duration,
}

/// Outcome of a single token poll.
pub enum Poll {
    Pending,
    Approved(String),
    Denied,
    Expired,
    Consumed,
}

fn http() -> ureq::Agent {
    ureq::AgentBuilder::new()
        .timeout(Duration::from_secs(10))
        .build()
}

/// Begin a pairing. Returns the device code + the human code/link to approve.
pub fn start(agent: &ureq::Agent, api_base: &str) -> Result<Started> {
    let api_base = api_base.trim_end_matches('/');
    let resp: StartResponse = agent
        .post(&format!("{api_base}/agent/pair/start"))
        .send_json(serde_json::json!({ "label": device_label() }))
        .context("could not reach the backend to start pairing")?
        .into_json()
        .context("unexpected response from pair/start")?;
    Ok(Started {
        device_code: resp.device_code,
        user_code: resp.user_code,
        verification_uri: resp.verification_uri,
        verification_uri_complete: resp.verification_uri_complete,
        interval: Duration::from_secs(resp.interval_seconds.max(MIN_INTERVAL_SECS)),
        expires_in: Duration::from_secs(resp.expires_in_seconds.max(MIN_EXPIRY_SECS)),
    })
}

/// Poll once for approval. `Approved` carries the one-time API key.
pub fn poll_once(agent: &ureq::Agent, api_base: &str, device_code: &str) -> Result<Poll> {
    let api_base = api_base.trim_end_matches('/');
    let resp: TokenResponse = agent
        .post(&format!("{api_base}/agent/pair/token"))
        .send_json(serde_json::json!({ "deviceCode": device_code }))
        .context("pair/token request failed")?
        .into_json()
        .context("unexpected response from pair/token")?;
    Ok(match resp.status.as_str() {
        "pending" => Poll::Pending,
        "approved" => Poll::Approved(
            resp.api_key
                .ok_or_else(|| anyhow!("backend approved the pairing but returned no api_key"))?,
        ),
        "denied" => Poll::Denied,
        "expired" => Poll::Expired,
        "consumed" => Poll::Consumed,
        other => return Err(anyhow!("unexpected pairing status '{other}'")),
    })
}

/// The interactive CLI flow (`acrally-agent pair`): prints the code/link, polls,
/// and writes the key to the config file. The tray UI is the preferred path; this
/// stays as a headless fallback.
pub fn run(cfg: &Config) -> Result<()> {
    let agent = http();
    let started = start(&agent, &cfg.api_base)?;

    println!();
    println!("  ┌─ Link this agent to your account ─────────────────────────");
    println!("  │");
    println!("  │  1. Open:  {}", started.verification_uri_complete);
    println!("  │  2. Confirm the code:  {}", started.user_code);
    println!("  │");
    println!("  │  (or go to {} and enter {})", started.verification_uri, started.user_code);
    println!("  └───────────────────────────────────────────────────────────");
    println!();
    if open_browser(&started.verification_uri_complete).is_ok() {
        println!("  Opened your browser. Waiting for approval…");
    } else {
        println!("  Waiting for approval…");
    }

    let deadline = Instant::now() + started.expires_in;
    loop {
        if Instant::now() > deadline {
            return Err(anyhow!("pairing timed out — run `acrally-agent pair` again"));
        }
        std::thread::sleep(started.interval);
        match poll_once(&agent, &cfg.api_base, &started.device_code) {
            Ok(Poll::Pending) => continue,
            Ok(Poll::Approved(key)) => {
                let path = Config::config_path();
                persist_api_key(&path, &key)
                    .with_context(|| format!("could not write api_key to '{path}'"))?;
                println!();
                println!("  ✓ Agent authorized. Key saved to {path}.");
                println!("    Run the agent normally now — it will report your runs to the club.");
                return Ok(());
            }
            Ok(Poll::Denied) => return Err(anyhow!("pairing was denied in the browser")),
            Ok(Poll::Expired) => {
                return Err(anyhow!("the code expired — run `acrally-agent pair` again"))
            }
            Ok(Poll::Consumed) => return Err(anyhow!("this pairing was already used")),
            // Transient network error — keep trying until the deadline.
            Err(e) => eprintln!("  (still waiting… {e})"),
        }
    }
}

/// A human-friendly device label shown on the approval page (host + agent version).
fn device_label() -> String {
    let host = std::env::var("COMPUTERNAME")
        .or_else(|_| std::env::var("HOSTNAME"))
        .unwrap_or_else(|_| "PC".to_string());
    format!("{host} (acrally-agent {})", env!("CARGO_PKG_VERSION"))
}

/// Best-effort browser open; failure is fine (we always show the link too).
pub fn open_browser(url: &str) -> std::io::Result<()> {
    #[cfg(windows)]
    {
        std::process::Command::new("cmd")
            .args(["/C", "start", "", url])
            .spawn()
            .map(|_| ())
    }
    #[cfg(target_os = "macos")]
    {
        std::process::Command::new("open").arg(url).spawn().map(|_| ())
    }
    #[cfg(all(unix, not(target_os = "macos")))]
    {
        std::process::Command::new("xdg-open").arg(url).spawn().map(|_| ())
    }
    #[cfg(not(any(windows, unix)))]
    {
        let _ = url;
        Ok(())
    }
}

/// Write `api_key` into the TOML config, replacing an existing (possibly commented)
/// `api_key` line in place, otherwise appending one. Line-based so surrounding
/// comments are preserved (round-tripping TOML through serde would drop them).
pub fn persist_api_key(path: &str, key: &str) -> Result<()> {
    // The config file may not exist yet (nothing needs hand-editing) — start from
    // empty in that case and create it below.
    let text = match std::fs::read_to_string(path) {
        Ok(text) => text,
        Err(e) if e.kind() == std::io::ErrorKind::NotFound => String::new(),
        Err(e) => return Err(e).with_context(|| format!("could not read '{path}'")),
    };
    let new_line = format!("api_key = \"{key}\"");

    let mut replaced = false;
    let mut out: Vec<String> = Vec::new();
    for line in text.lines() {
        let is_api_key_line = line
            .trim_start()
            .trim_start_matches('#')
            .trim_start()
            .starts_with("api_key");
        if is_api_key_line {
            if !replaced {
                out.push(new_line.clone());
                replaced = true;
            }
            // Drop any further api_key lines (real or commented).
        } else {
            out.push(line.to_string());
        }
    }
    if !replaced {
        out.push(new_line);
    }

    let mut content = out.join("\n");
    if text.ends_with('\n') {
        content.push('\n');
    }
    if let Some(parent) = std::path::Path::new(path).parent() {
        if !parent.as_os_str().is_empty() {
            std::fs::create_dir_all(parent)
                .with_context(|| format!("could not create config dir '{}'", parent.display()))?;
        }
    }
    std::fs::write(path, content).with_context(|| format!("could not write '{path}'"))?;
    Ok(())
}

// ---- UI-driven pairing (tray app Connect screen) ----

/// Live phase of an in-progress pairing, published to the UI thread.
#[cfg(feature = "ui")]
#[derive(Clone, Default)]
pub enum Phase {
    /// Not started yet.
    #[default]
    Idle,
    /// Contacting the backend to obtain a code.
    Connecting,
    /// Showing the code/link, polling for approval.
    Waiting { user_code: String, url: String },
    /// Approved — carries the one-time key (also written to config).
    Approved { api_key: String },
    /// Something went wrong; carries a user-facing message.
    Failed(String),
}

/// Drive a full pairing to completion, publishing progress into `phase` for the UI.
/// Runs on its own thread so it never blocks the event loop.
#[cfg(feature = "ui")]
pub fn drive(api_base: String, phase: std::sync::Arc<std::sync::Mutex<Phase>>) {
    let set = |p: Phase| {
        if let Ok(mut guard) = phase.lock() {
            *guard = p;
        }
    };

    set(Phase::Connecting);
    let agent = http();
    let started = match start(&agent, &api_base) {
        Ok(s) => s,
        Err(e) => return set(Phase::Failed(friendly(&e))),
    };

    set(Phase::Waiting {
        user_code: started.user_code.clone(),
        url: started.verification_uri_complete.clone(),
    });
    let _ = open_browser(&started.verification_uri_complete);

    let deadline = Instant::now() + started.expires_in;
    loop {
        if Instant::now() > deadline {
            return set(Phase::Failed("The code expired. Try connecting again.".into()));
        }
        std::thread::sleep(started.interval);
        match poll_once(&agent, &api_base, &started.device_code) {
            Ok(Poll::Pending) => continue,
            Ok(Poll::Approved(key)) => {
                let path = Config::config_path();
                if let Err(e) = persist_api_key(&path, &key) {
                    return set(Phase::Failed(format!("Linked, but couldn't save the key: {e}")));
                }
                return set(Phase::Approved { api_key: key });
            }
            Ok(Poll::Denied) => return set(Phase::Failed("Pairing was denied.".into())),
            Ok(Poll::Expired) => {
                return set(Phase::Failed("The code expired. Try connecting again.".into()))
            }
            Ok(Poll::Consumed) => return set(Phase::Failed("That code was already used.".into())),
            // Transient network error — keep trying until the deadline.
            Err(_) => continue,
        }
    }
}

#[cfg(feature = "ui")]
fn friendly(e: &anyhow::Error) -> String {
    format!("Couldn't reach the club backend. Check your connection and try again. ({e})")
}
