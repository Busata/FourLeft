//! Agent configuration.
//!
//! In normal use there is nothing to hand-edit: the backend URL is baked in and
//! the API key is written by `acrally-agent pair` (see `pairing.rs`). The config
//! file is therefore optional — when it's missing every field falls back to its
//! default. It exists mainly so pairing has somewhere to persist the key, plus a
//! few dev/self-host escape hatches (`api_base`, `save_path`, `mock`) that are
//! also settable via `ACRALLY_*` env vars.

use std::path::PathBuf;
use std::time::Duration;

use anyhow::{Context, Result};
use serde::Deserialize;

/// The club backend the agent talks to. Overridable via the config file or the
/// `ACRALLY_API_BASE` env var for dev / self-hosting.
const DEFAULT_API_BASE: &str = "https://fourleft.io/acrally-api";

/// Internal timing — how the pipeline is tuned. Not user-configurable.
const POLL_HZ: f64 = 10.0;
const HEARTBEAT_SECS: f64 = 1.0;
const FINISH_SECS: f64 = 1.5;
const SAVE_WAIT_SECS: f64 = 60.0;

#[derive(Debug, Clone, Deserialize)]
pub struct Config {
    /// Base URL of the club backend. Defaults to the fourleft.io backend; only a
    /// dev/self-host setup needs to change it (config file or `ACRALLY_API_BASE`).
    #[serde(default = "default_api_base")]
    pub api_base: String,

    /// Bearer token sent as `Authorization: Bearer <api_key>`. Written by
    /// `acrally-agent pair`; not set by hand.
    #[serde(default)]
    pub api_key: Option<String>,

    /// Force the mock telemetry source even on Windows (useful for testing).
    /// Also forced by the `ACRALLY_MOCK` env var.
    #[serde(default)]
    pub mock: bool,

    /// Run without the tray/window UI, logging to the console instead. Only
    /// relevant to a build with the `ui` feature; a non-`ui` build is always
    /// headless. Also forced by the `ACRALLY_HEADLESS` env var.
    #[serde(default)]
    pub headless: bool,

    /// Print a once-per-second heartbeat (status, speed, gear, lap time) so you
    /// can confirm the agent is reading the game while driving. Also forced by
    /// the `ACRALLY_VERBOSE` env var.
    #[serde(default)]
    pub verbose: bool,

    /// Explicit path to `PlayerDataSaveSlot.sav`. If unset, it is auto-detected
    /// under `%LOCALAPPDATA%\acr\Saved\SaveGames`.
    #[serde(default)]
    pub save_path: Option<String>,
}

fn default_api_base() -> String {
    DEFAULT_API_BASE.to_string()
}

/// Per-user config/data directory for the agent:
/// `%LOCALAPPDATA%\acrally` on Windows, `$XDG_CONFIG_HOME/acrally` (or
/// `~/.config/acrally`) elsewhere, falling back to the current directory.
pub fn config_dir() -> PathBuf {
    #[cfg(windows)]
    {
        if let Ok(base) = std::env::var("LOCALAPPDATA") {
            return PathBuf::from(base).join("acrally");
        }
    }
    #[cfg(not(windows))]
    {
        if let Ok(base) = std::env::var("XDG_CONFIG_HOME") {
            return PathBuf::from(base).join("acrally");
        }
        if let Ok(home) = std::env::var("HOME") {
            return PathBuf::from(home).join(".config").join("acrally");
        }
    }
    PathBuf::from(".")
}

impl Config {
    /// The config file path. `$ACRALLY_CONFIG` overrides; otherwise `config.toml`
    /// in the per-user config dir (see [`config_dir`]) — a stable location that
    /// survives the exe being moved or self-updated, unlike the working directory.
    pub fn config_path() -> String {
        if let Ok(p) = std::env::var("ACRALLY_CONFIG") {
            return p;
        }
        config_dir()
            .join("config.toml")
            .to_string_lossy()
            .into_owned()
    }

    /// Load config. A missing file is fine — every field has a default, so the
    /// agent runs on defaults and pairing can create the file later. Reads
    /// `$ACRALLY_CONFIG` if set, otherwise `config.toml` in the CWD.
    pub fn load() -> Result<Self> {
        let path = Self::config_path();
        let text = match std::fs::read_to_string(&path) {
            Ok(text) => text,
            Err(e) if e.kind() == std::io::ErrorKind::NotFound => String::new(),
            Err(e) => {
                return Err(e).with_context(|| format!("could not read config file '{path}'"))
            }
        };
        let mut cfg: Config =
            toml::from_str(&text).with_context(|| format!("invalid TOML in '{path}'"))?;

        if let Ok(api_base) = std::env::var("ACRALLY_API_BASE") {
            cfg.api_base = api_base;
        }
        if std::env::var("ACRALLY_MOCK").is_ok() {
            cfg.mock = true;
        }
        if std::env::var("ACRALLY_VERBOSE").is_ok() {
            cfg.verbose = true;
        }
        if std::env::var("ACRALLY_HEADLESS").is_ok() {
            cfg.headless = true;
        }
        Ok(cfg)
    }

    pub fn poll_interval(&self) -> Duration {
        Duration::from_secs_f64(1.0 / POLL_HZ)
    }

    /// Frames of a frozen timer that count as a finished run.
    pub fn finish_frames(&self) -> u32 {
        ((FINISH_SECS * POLL_HZ).round() as u32).max(1)
    }

    pub fn heartbeat_interval(&self) -> Duration {
        Duration::from_secs_f64(HEARTBEAT_SECS)
    }

    pub fn save_wait(&self) -> Duration {
        Duration::from_secs_f64(SAVE_WAIT_SECS)
    }
}
