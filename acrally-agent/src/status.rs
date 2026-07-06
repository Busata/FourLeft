//! A shared, live snapshot of what the agent is doing, written by the telemetry
//! thread and read by the UI thread.
//!
//! Only the GUI build reads most of these fields, so allow dead code on the
//! headless build (which threads a `None` status through the runner).
#![allow(dead_code)]

use std::sync::{Arc, Mutex};

use crate::model::{is_real_laptime, Frame, SimStatus};

/// What the driver is doing right now, derived from a telemetry frame.
#[derive(Debug, Clone, Copy, PartialEq, Eq, Default)]
pub enum DriveState {
    /// Game not detected (shared memory unavailable).
    #[default]
    NoGame,
    /// In menus / stationary.
    Idle,
    /// Actively driving a stage.
    Driving,
    /// Watching a replay.
    Replay,
    /// Simulation paused.
    Paused,
    /// Status word we don't recognise.
    Unknown,
}

impl DriveState {
    pub fn label(self) -> &'static str {
        match self {
            DriveState::NoGame => "game not detected",
            DriveState::Idle => "idle",
            DriveState::Driving => "driving",
            DriveState::Replay => "replay",
            DriveState::Paused => "paused",
            DriveState::Unknown => "unknown",
        }
    }
}

/// Live snapshot shared between the telemetry thread (writer) and UI (reader).
#[derive(Debug, Clone, Default)]
pub struct AgentStatus {
    // Set once at startup.
    pub source: String,
    pub backend: String,
    pub save_path: Option<String>,
    pub agent_version: String,

    // Live telemetry, refreshed every poll by the telemetry thread.
    pub state: DriveState,
    pub track: String,
    pub car: String,
    pub driver: String,
    pub speed_kmh: f32,
    pub gear: i32,
    pub rpm: i32,
    pub current_laptime: String,
    pub distance_m: f32,

    // Session / backend, updated by the runner at lifecycle points.
    pub session_id: Option<String>,
    pub backend_connected: bool,
    pub last_result: Option<String>,
    pub sessions_started: u64,
    pub results_posted: u64,
}

/// Handle shared across threads.
pub type StatusHandle = Arc<Mutex<AgentStatus>>;

impl AgentStatus {
    /// Refresh the live-telemetry fields from a frame.
    pub fn apply_frame(&mut self, f: &Frame) {
        self.state = derive_state(f);
        self.track = f.track.clone();
        self.car = f.car.clone();
        self.driver = f.driver.clone();
        self.speed_kmh = f.speed_kmh;
        self.gear = f.gear;
        self.rpm = f.rpm;
        self.current_laptime = f.current_laptime.clone();
        self.distance_m = f.distance_m;
    }

    /// Mark that the game is no longer being read.
    pub fn mark_no_game(&mut self) {
        self.state = DriveState::NoGame;
        self.speed_kmh = 0.0;
    }
}

/// Derive the displayed state from a frame. AC Rally's `status` enum is
/// unreliable (reads "off" while driving), so a running timer or real speed wins.
fn derive_state(f: &Frame) -> DriveState {
    if is_real_laptime(&f.current_laptime) || f.speed_kmh > 1.0 {
        return DriveState::Driving;
    }
    match f.status {
        SimStatus::Off | SimStatus::Live => DriveState::Idle,
        SimStatus::Replay => DriveState::Replay,
        SimStatus::Pause => DriveState::Paused,
        SimStatus::Unknown(_) => DriveState::Unknown,
    }
}
