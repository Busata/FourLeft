//! Races tab client: the "arm & register" control channel (UI build only).
//!
//! The agent shows the open events of the clubs the driver belongs to and lets them press **Start**
//! on a specific stage. Arming is server-side (see the backend's `event_arm`): the arm binds to the
//! driver's next run, so a run already in progress when Start is pressed can never be captured. This
//! module is just the HTTP client + a small shared state a background poller keeps fresh.
//!
//!   GET  {api_base}/agent/races          -> { events, arm }
//!   POST {api_base}/agent/races/arm      { event_id, variant_id } -> arm state
//!   POST {api_base}/agent/races/disarm   -> arm state
//!
//! All calls carry `Authorization: Bearer <api_key>`, like the ingestion endpoints.

#![cfg(feature = "ui")]

use std::sync::atomic::{AtomicBool, Ordering};
use std::sync::{Arc, Mutex};
use std::time::Duration;

use anyhow::{anyhow, Context, Result};
use serde::Deserialize;

use crate::config::Config;

/// How often the background poller refreshes the events + arm state while the app is open.
const POLL_INTERVAL: Duration = Duration::from_secs(4);

#[derive(Clone, Debug, Default, Deserialize)]
pub struct RacesView {
    #[serde(default)]
    pub events: Vec<RaceEvent>,
    #[serde(default)]
    pub arm: ArmState,
}

#[derive(Clone, Debug, Deserialize)]
pub struct RaceEvent {
    pub event_id: String,
    #[allow(dead_code)]
    pub championship_id: String,
    pub championship_name: String,
    pub club_name: String,
    pub label: String,
    #[allow(dead_code)]
    pub opens_at: String,
    pub closes_at: String,
    #[serde(default)]
    pub stages: Vec<RaceStage>,
}

#[derive(Clone, Debug, Deserialize)]
pub struct RaceStage {
    pub variant_id: String,
    // Kept from the wire for completeness / future use, even where the UI shows only `label`.
    #[serde(default)]
    #[allow(dead_code)]
    pub raw_name: Option<String>,
    pub label: String,
    #[serde(default)]
    #[allow(dead_code)]
    pub stage_name: Option<String>,
    #[serde(default)]
    #[allow(dead_code)]
    pub location_name: Option<String>,
    /// Readable catalogue names of the permitted cars (for display).
    #[serde(default)]
    pub cars: Vec<String>,
    /// Raw strings the game may report for those cars (names + aliases), matched against telemetry.
    #[serde(default)]
    #[allow(dead_code)]
    pub car_keys: Vec<String>,
    #[serde(default)]
    pub my_best_ms: Option<i64>,
}

#[derive(Clone, Debug, Default, Deserialize)]
pub struct ArmState {
    #[serde(default)]
    pub active: bool,
    #[serde(default)]
    pub status: Option<String>,
    // The armed event id — carried for completeness; the UI keys off variant_id.
    #[serde(default)]
    #[allow(dead_code)]
    pub event_id: Option<String>,
    #[serde(default)]
    pub variant_id: Option<String>,
    #[serde(default)]
    pub stage_label: Option<String>,
    #[serde(default)]
    pub raw_name: Option<String>,
    /// Readable permitted car names for the armed stage (display).
    #[serde(default)]
    pub cars: Vec<String>,
    /// Raw car strings the game may report for the permitted cars (matched against telemetry).
    #[serde(default)]
    pub car_keys: Vec<String>,
    #[serde(default)]
    pub last_outcome: Option<String>,
    #[serde(default)]
    pub last_stage_label: Option<String>,
    #[serde(default)]
    pub last_total_ms: Option<i64>,
}

/// Shared state the UI reads and the poller/actions write.
#[derive(Clone, Default)]
pub struct RacesState {
    pub view: RacesView,
    pub loading: bool,
    pub loaded_once: bool,
    pub error: Option<String>,
    /// Set while an arm/disarm POST is in flight, to disable the buttons.
    pub busy: bool,
}

pub type RacesHandle = Arc<Mutex<RacesState>>;

fn http() -> ureq::Agent {
    ureq::AgentBuilder::new()
        .timeout(Duration::from_secs(10))
        .build()
}

struct Client {
    api_base: String,
    api_key: Option<String>,
    agent: ureq::Agent,
}

impl Client {
    fn new(cfg: &Config) -> Self {
        Client {
            api_base: cfg.api_base.trim_end_matches('/').to_string(),
            api_key: cfg.api_key.clone(),
            agent: http(),
        }
    }

    fn req(&self, req: ureq::Request) -> ureq::Request {
        match &self.api_key {
            Some(key) => req.set("Authorization", &format!("Bearer {key}")),
            None => req,
        }
    }

    fn fetch(&self) -> Result<RacesView> {
        self.req(self.agent.get(&format!("{}/agent/races", self.api_base)))
            .call()
            .context("could not reach the club backend")?
            .into_json()
            .context("unexpected response from the races endpoint")
    }

    fn arm(&self, event_id: &str, variant_id: &str) -> Result<ArmState> {
        self.req(self.agent.post(&format!("{}/agent/races/arm", self.api_base)))
            .send_json(serde_json::json!({ "event_id": event_id, "variant_id": variant_id }))
            .map_err(arm_error)?
            .into_json()
            .context("unexpected response arming the stage")
    }

    fn disarm(&self) -> Result<ArmState> {
        self.req(self.agent.post(&format!("{}/agent/races/disarm", self.api_base)))
            .call()
            .context("could not reach the club backend")?
            .into_json()
            .context("unexpected response from disarm")
    }
}

/// Turn a ureq error into a friendly message, surfacing the backend's reason on a 4xx.
fn arm_error(e: ureq::Error) -> anyhow::Error {
    match e {
        ureq::Error::Status(_, resp) => {
            let body = resp.into_string().unwrap_or_default();
            let reason = body.trim();
            if reason.is_empty() {
                anyhow!("could not arm the stage")
            } else {
                anyhow!(reason.to_string())
            }
        }
        other => anyhow!("could not reach the club backend ({other})"),
    }
}

/// Refresh the shared view once (synchronous — call from a worker thread).
fn refresh_into(client: &Client, handle: &RacesHandle) {
    let result = client.fetch();
    if let Ok(mut state) = handle.lock() {
        state.loading = false;
        state.loaded_once = true;
        match result {
            Ok(view) => {
                state.view = view;
                state.error = None;
            }
            Err(e) => state.error = Some(e.to_string()),
        }
    }
}

/// Background poller: keep the events + arm state fresh while the app runs. Stops when `stop` is set.
pub fn poller(cfg: Config, handle: RacesHandle, stop: Arc<AtomicBool>) {
    let client = Client::new(&cfg);
    if let Ok(mut state) = handle.lock() {
        state.loading = true;
    }
    while !stop.load(Ordering::Relaxed) {
        refresh_into(&client, &handle);
        // Sleep in small slices so quitting is responsive.
        let mut slept = Duration::ZERO;
        while slept < POLL_INTERVAL && !stop.load(Ordering::Relaxed) {
            std::thread::sleep(Duration::from_millis(200));
            slept += Duration::from_millis(200);
        }
    }
}

/// Arm a stage, then refresh. Runs on its own thread so the UI never blocks.
pub fn arm(cfg: Config, handle: RacesHandle, event_id: String, variant_id: String) {
    set_busy(&handle, true);
    std::thread::spawn(move || {
        let client = Client::new(&cfg);
        match client.arm(&event_id, &variant_id) {
            Ok(arm) => {
                if let Ok(mut state) = handle.lock() {
                    state.view.arm = arm;
                    state.error = None;
                }
            }
            Err(e) => {
                if let Ok(mut state) = handle.lock() {
                    state.error = Some(e.to_string());
                }
            }
        }
        refresh_into(&client, &handle);
        set_busy(&handle, false);
    });
}

/// Disarm, then refresh. Runs on its own thread.
pub fn disarm(cfg: Config, handle: RacesHandle) {
    set_busy(&handle, true);
    std::thread::spawn(move || {
        let client = Client::new(&cfg);
        match client.disarm() {
            Ok(arm) => {
                if let Ok(mut state) = handle.lock() {
                    state.view.arm = arm;
                    state.error = None;
                }
            }
            Err(e) => {
                if let Ok(mut state) = handle.lock() {
                    state.error = Some(e.to_string());
                }
            }
        }
        refresh_into(&client, &handle);
        set_busy(&handle, false);
    });
}

fn set_busy(handle: &RacesHandle, busy: bool) {
    if let Ok(mut state) = handle.lock() {
        state.busy = busy;
    }
}
