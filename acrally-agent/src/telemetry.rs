//! The `TelemetrySource` abstraction plus a platform-independent mock source.
//!
//! The real Windows shared-memory reader lives in [`crate::shm`] and is only
//! compiled on Windows. Everything else in the agent depends on this trait, so
//! the detect -> submit pipeline is fully exercisable on any OS via [`MockSource`].

use crate::model::{Frame, SimStatus};

/// A source of normalized telemetry frames.
pub trait TelemetrySource {
    /// Latest frame, or `None` when the sim isn't running / mapping unavailable.
    fn poll(&mut self) -> Option<Frame>;

    /// Human-readable name for logging.
    fn name(&self) -> &'static str;
}

/// A scripted telemetry source that replays a fixed sequence of frames on a
/// loop, so the whole pipeline can be run without the game (and without Windows).
///
/// The built-in scenario simulates a driver completing successive stage runs
/// with varying times — enough to see the detector fire and the submitter POST.
pub struct MockSource {
    frames: Vec<Frame>,
    idx: usize,
}

impl MockSource {
    pub fn new() -> Self {
        MockSource {
            frames: build_scenario(),
            idx: 0,
        }
    }
}

impl TelemetrySource for MockSource {
    fn poll(&mut self) -> Option<Frame> {
        let f = self.frames[self.idx % self.frames.len()].clone();
        self.idx += 1;
        Some(f)
    }

    fn name(&self) -> &'static str {
        "mock"
    }
}

/// Build a small scenario: menu -> live driving -> two completed laps -> back to
/// menu, then it loops. Each completed lap surfaces a new `last_laptime`, which
/// is exactly what the detector keys on.
fn build_scenario() -> Vec<Frame> {
    let base = |status: SimStatus, current_lap: i32, last: &str, invalid: bool| Frame {
        status,
        session_type: 0, // time attack
        current_lap,
        end_session: false,
        current_laptime: if matches!(status, SimStatus::Live) {
            "1:12.345".to_string()
        } else {
            "--:--".to_string()
        },
        last_laptime: last.to_string(),
        best_laptime: "2:58.113".to_string(),
        current_time_ms: if matches!(status, SimStatus::Live) {
            72_345
        } else {
            0
        },
        last_time_ms: crate::model::parse_laptime_ms(last).unwrap_or(0) as i32,
        best_time_ms: 178_113,
        completed_laps: (current_lap - 1).max(0),
        current_sector: 0,
        distance_m: 0.0,
        is_invalid: invalid,
        speed_kmh: if matches!(status, SimStatus::Live) {
            92.4
        } else {
            0.0
        },
        gear: if matches!(status, SimStatus::Live) {
            3
        } else {
            0
        },
        rpm: if matches!(status, SimStatus::Live) {
            5400
        } else {
            0
        },
        driver: "Dries Desmet".to_string(),
        car: "Lancia Delta HF Integrale".to_string(),
        track: "Col de Turini".to_string(),
        track_config: "Descent".to_string(),
    };

    vec![
        // Sitting in the menu.
        base(SimStatus::Off, 0, "--:--", false),
        // Lap 1 in progress, no completed time yet.
        base(SimStatus::Live, 1, "--:--", false),
        base(SimStatus::Live, 1, "--:--", false),
        // Lap 1 completes -> new last_laptime appears (clean lap).
        base(SimStatus::Live, 2, "3:01.402", false),
        base(SimStatus::Live, 2, "3:01.402", false),
        // Lap 2 in progress, driver runs wide (lap gets invalidated).
        base(SimStatus::Live, 2, "3:01.402", true),
        // Lap 2 completes -> new last_laptime (marked invalid because it was dirtied).
        base(SimStatus::Live, 3, "2:58.740", false),
        base(SimStatus::Live, 3, "2:58.740", false),
        // Back to the menu; detector resets so the next session re-emits.
        base(SimStatus::Off, 0, "--:--", false),
    ]
}
