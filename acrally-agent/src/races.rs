//! Races client: the "arm & register" control channel.
//!
//! The agent shows the open events of the clubs the driver belongs to and lets them press **Start**
//! on a specific stage. Arming is server-side (see the backend's `event_arm`): the arm binds to the
//! driver's next run, so a run already in progress when Start is pressed can never be captured. This
//! module is the HTTP client plus two front-ends over it: the UI's races tab (a small shared state
//! a background poller keeps fresh) and the `arm-list` / `arm` / `disarm` console commands, which
//! give a headless agent (e.g. under Wine with no display) the same control.
//!
//!   GET  {api_base}/agent/races          -> { events, arm }
//!   POST {api_base}/agent/races/arm      { event_id, variant_id } -> arm state
//!   POST {api_base}/agent/races/disarm   -> arm state
//!
//! All calls carry `Authorization: Bearer <api_key>`, like the ingestion endpoints.

#[cfg(feature = "ui")]
use std::sync::atomic::{AtomicBool, Ordering};
#[cfg(feature = "ui")]
use std::sync::{Arc, Mutex};
use std::time::Duration;

use anyhow::{anyhow, bail, Context, Result};
use serde::Deserialize;

use crate::config::Config;
use crate::model::fmt_ms;

/// How often the background poller refreshes the events + arm state while the app is open.
#[cfg(feature = "ui")]
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
    /// One shot per stage: set when the driver's attempt is spent (a recorded time, or a DNF
    /// expiry when `my_best_ms` is absent). The backend refuses to re-arm a completed stage.
    #[serde(default)]
    pub completed: bool,
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
    /// Raw car strings the game may report for the permitted cars (matched
    /// against telemetry by the UI's wrong-car warning; unused headless).
    #[serde(default)]
    #[allow(dead_code)]
    pub car_keys: Vec<String>,
    #[serde(default)]
    pub last_outcome: Option<String>,
    #[serde(default)]
    pub last_stage_label: Option<String>,
    #[serde(default)]
    pub last_total_ms: Option<i64>,
}

/// Shared state the UI reads and the poller/actions write.
#[cfg(feature = "ui")]
#[derive(Clone, Default)]
pub struct RacesState {
    pub view: RacesView,
    pub loading: bool,
    pub loaded_once: bool,
    pub error: Option<String>,
    /// Set while an arm/disarm POST is in flight, to disable the buttons.
    pub busy: bool,
}

#[cfg(feature = "ui")]
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
        self.req(
            self.agent
                .post(&format!("{}/agent/races/arm", self.api_base)),
        )
        .send_json(serde_json::json!({ "event_id": event_id, "variant_id": variant_id }))
        .map_err(arm_error)?
        .into_json()
        .context("unexpected response arming the stage")
    }

    fn disarm(&self) -> Result<ArmState> {
        self.req(
            self.agent
                .post(&format!("{}/agent/races/disarm", self.api_base)),
        )
        .call()
        // Surface the backend's reason on a 4xx — disarm is refused (409) while
        // a bound run is in progress, and that message should reach the driver.
        .map_err(arm_error)?
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
#[cfg(feature = "ui")]
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
#[cfg(feature = "ui")]
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
#[cfg(feature = "ui")]
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
#[cfg(feature = "ui")]
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

#[cfg(feature = "ui")]
fn set_busy(handle: &RacesHandle, busy: bool) {
    if let Ok(mut state) = handle.lock() {
        state.busy = busy;
    }
}

// ---------------------------------------------------------------------------
// Console front-end: `arm-list`, `arm <n>`, `disarm`. Same primitives as the
// races tab, for a headless agent (or any terminal user). These run and exit;
// the arm lives on the server, so a concurrently running agent needs no signal.
// ---------------------------------------------------------------------------

/// A paired key is required before the backend will answer any races call —
/// fail fast with the fix instead of surfacing a bare 401.
fn require_key(cfg: &Config) -> Result<()> {
    if cfg.api_key.is_none() {
        bail!("not paired — run `acrally-agent pair` first, then try again.");
    }
    Ok(())
}

/// `acrally-agent arm-list`: current arm state, then every open stage with a
/// pick number for `arm <n>`.
pub fn run_list(cfg: &Config) -> Result<()> {
    require_key(cfg)?;
    let client = Client::new(cfg);
    let view = client.fetch()?;
    print_arm(&view.arm);
    if view.events.is_empty() {
        println!("no open events — join a club championship on the website first.");
        return Ok(());
    }
    let mut n = 0usize;
    for event in &view.events {
        println!();
        println!(
            "{} — {} · {}  (closes {})",
            event.label, event.championship_name, event.club_name, event.closes_at
        );
        for stage in &event.stages {
            n += 1;
            let best = stage
                .my_best_ms
                .map(|ms| format!("  best {}", fmt_ms(ms as i32)))
                .unwrap_or_default();
            let armed = if view.arm.active
                && view.arm.variant_id.as_deref() == Some(stage.variant_id.as_str())
            {
                "  [ARMED]"
            } else if stage.completed && stage.my_best_ms.is_none() {
                "  [DNF]"
            } else if stage.completed {
                "  [DONE]"
            } else {
                ""
            };
            println!("  [{n}] {}{best}{armed}", stage.label);
            if !stage.cars.is_empty() {
                println!("       cars: {}", stage.cars.join(", "));
            }
        }
    }
    println!();
    println!("arm one with: acrally-agent arm <number>");
    Ok(())
}

/// `acrally-agent arm <selector>`: arm a stage by its `arm-list` number, or by
/// variant id for scripting.
pub fn run_arm(cfg: &Config, selector: &str) -> Result<()> {
    require_key(cfg)?;
    let client = Client::new(cfg);
    let view = client.fetch()?;
    // Flattened in the same order `run_list` numbers them.
    let flat: Vec<(&RaceEvent, &RaceStage)> = view
        .events
        .iter()
        .flat_map(|e| e.stages.iter().map(move |s| (e, s)))
        .collect();
    let picked = match selector.parse::<usize>() {
        Ok(n) => n.checked_sub(1).and_then(|i| flat.get(i)),
        Err(_) => flat.iter().find(|(_, s)| s.variant_id == selector),
    };
    let Some((event, stage)) = picked else {
        bail!("no stage '{selector}' — run `acrally-agent arm-list` to see the numbers.");
    };
    if stage.completed {
        bail!("that stage is already done — one shot per stage.");
    }
    println!(
        "arming {} ({} · {})...",
        stage.label, event.label, event.club_name
    );
    let arm = client.arm(&event.event_id, &stage.variant_id)?;
    print_arm(&arm);
    Ok(())
}

/// `acrally-agent disarm`: release the current arm (refused by the backend
/// while a bound run is in progress).
pub fn run_disarm(cfg: &Config) -> Result<()> {
    require_key(cfg)?;
    let client = Client::new(cfg);
    let arm = client.disarm()?;
    print_arm(&arm);
    Ok(())
}

/// One-paragraph console rendering of an arm state: what's armed (with car
/// restrictions), or the previous run's authoritative outcome. Mirrors the UI's
/// arm/outcome banners.
fn print_arm(arm: &ArmState) {
    if arm.active {
        let stage = arm
            .stage_label
            .clone()
            .or_else(|| arm.raw_name.clone())
            .unwrap_or_else(|| "a stage".to_string());
        println!("ARMED: {stage} — your next completed run counts.");
        if !arm.cars.is_empty() {
            println!("  cars: {}", arm.cars.join(", "));
        }
        if let Some(status) = &arm.status {
            println!("  status: {status}");
        }
        return;
    }
    let stage = arm
        .last_stage_label
        .clone()
        .unwrap_or_else(|| "the stage".to_string());
    match arm.last_outcome.as_deref() {
        None => println!("not armed."),
        Some("RECORDED") => {
            let time = arm
                .last_total_ms
                .map(|ms| fmt_ms(ms as i32))
                .unwrap_or_default();
            println!("not armed. last run: recorded {time} on {stage}.");
        }
        Some("SLOWER") => {
            println!("not armed. last run: slower — kept your existing time on {stage}.")
        }
        Some("WRONG_STAGE") => {
            println!("not armed. last run: wasn't {stage} — nothing recorded.")
        }
        Some("WRONG_CAR") => {
            println!("not armed. last run: wrong car for {stage} — nothing recorded.")
        }
        Some("EVENT_CLOSED") => {
            println!("not armed. last run: the event closed before it finished — nothing recorded.")
        }
        Some("DNF") => {
            println!("not armed. last entry on {stage} expired without a run — recorded as DNF.")
        }
        Some(other) => println!("not armed. last run: {other}."),
    }
}
