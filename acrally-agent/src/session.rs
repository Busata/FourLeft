//! Session state machine, driven by the live shared-memory stage timer.
//!
//! Rally's `status` enum and integer timers are unreliable, but the wchar
//! `current_laptime` string ticks accurately while driving. We key everything off
//! it (parsed to ms):
//!
//!   - timer starts running       -> `Start`     (open a session on the server)
//!   - timer advancing            -> `Progress`  (main throttles heartbeat posts)
//!   - timer resets to ~0 mid-run -> `Restart`   (abandon the session; a restart
//!                                                 writes no save record)
//!   - timer freezes / clears     -> `Finish`    (look up the penalised result in
//!                                                 the save file, then post it)
//!
//! `last_laptime` changing to a new real value outranks all of the above: the
//! game writes the completed stage time there at the finish line, at which point
//! the live timer resets and keeps ticking behind the result screen — exactly
//! the shape the drop-detector would misread as a `Restart` (aborting the run
//! that just earned a result). After a `Finish`, a new `Start` additionally
//! requires a blank gap first, so the result screen's ticking timer can't open
//! a phantom session.
//!
//! `Restart` is emitted at the moment a new run begins after a reset, so the
//! caller should abort the old session and open a fresh one.
//!
//! The string glitches (a blank frame, a brief freeze) mid-run, so `Finish` is
//! debounced (the freeze/blank must persist `finish_frames`) and gated on the run
//! having actually progressed (`peak_ms >= MIN_FINISH_MS`) — otherwise a single bad
//! read spawns phantom finish→abort→restart cycles during one continuous run.

use crate::model::{is_real_laptime, parse_laptime_ms, Frame};

#[derive(Debug, Clone, Copy, PartialEq)]
enum Phase {
    Idle,
    Running,
    Finished,
}

#[derive(Debug, Clone, Copy, PartialEq)]
pub enum SessionEvent {
    /// A new run began (timer started).
    Start,
    /// The run is ongoing (timer advancing).
    Progress,
    /// The run was restarted — abandon the current session; a new one has begun.
    Restart,
    /// The run ended (timer froze or cleared) — go find the result in the save.
    Finish,
}

pub struct SessionMachine {
    phase: Phase,
    last_ms: u32,
    /// Highest timer value seen in the current run — how far it has actually
    /// progressed. Guards against phantom finishes before a run gets going.
    peak_ms: u32,
    /// Consecutive frames the frozen timer has held its value.
    stable_frames: u32,
    /// Consecutive frames the timer has read blank (unparseable / zero).
    blank_frames: u32,
    /// Frames of a frozen (or blank) timer before we call the run finished.
    finish_frames: u32,
    /// `last_laptime` as it read when this run began. The game writes the
    /// completed stage time there the moment the car crosses the line, so a
    /// change to a new real value is the most direct finish signal we have.
    baseline_last: String,
    /// In `Finished`: whether a blank/no-telemetry gap has been seen since the
    /// finish. The stage timer can reset and keep ticking behind the result
    /// screen; without a gap that ticking would look like a fresh run.
    saw_gap: bool,
}

/// A drop of at least this much (ms) to ~0 signals a restart, not normal timing.
const RESTART_DROP_MS: u32 = 5_000;
/// A running timer below this (ms) counts as "a fresh run starting".
const FRESH_RUN_MS: u32 = 10_000;
/// A run must progress at least this far before a frozen/blank timer counts as a
/// finish. The live `current_laptime` string occasionally reads blank or low for a
/// frame or two; without this floor (and the debounce below) those glitches spawn
/// phantom finish→abort→restart cycles mid-run. No real stage finishes under 10s.
const MIN_FINISH_MS: u32 = 10_000;

impl SessionMachine {
    pub fn new(finish_frames: u32) -> Self {
        SessionMachine {
            phase: Phase::Idle,
            last_ms: 0,
            peak_ms: 0,
            stable_frames: 0,
            blank_frames: 0,
            finish_frames: finish_frames.max(1),
            baseline_last: String::new(),
            saw_gap: false,
        }
    }

    pub fn observe(&mut self, f: &Frame) -> Option<SessionEvent> {
        let cur = parse_laptime_ms(&f.current_laptime).map(|m| m as u32);

        match self.phase {
            Phase::Idle => {
                if let Some(ms) = cur {
                    self.enter_running(f, ms);
                    Some(SessionEvent::Start)
                } else {
                    None
                }
            }

            Phase::Running => {
                // Crossing the finish line writes the completed stage time into
                // `last_laptime`. This must be checked before the timer heuristics:
                // at the line the stage timer resets and keeps ticking behind the
                // result screen, which the drop-detector below would otherwise
                // misread as a Restart — aborting the very session that just earned
                // a result. `last_laptime` is only rewritten on completion (it is
                // not torn mid-run like the live timer string), so a single frame
                // with a new real value is trustworthy; the peak floor rules out a
                // stale value being mistaken for this run's finish.
                if is_real_laptime(&f.last_laptime)
                    && f.last_laptime != self.baseline_last
                    && self.peak_ms >= MIN_FINISH_MS
                {
                    self.phase = Phase::Finished;
                    self.saw_gap = false;
                    return Some(SessionEvent::Finish);
                }
                match cur {
                    // Timer read blank. A single blank frame is a glitch, not a finish;
                    // only a sustained clear (after a real run) ends the session.
                    None => {
                        self.blank_frames += 1;
                        if self.blank_frames >= self.finish_frames && self.peak_ms >= MIN_FINISH_MS
                        {
                            self.phase = Phase::Finished;
                            return Some(SessionEvent::Finish);
                        }
                        Some(SessionEvent::Progress)
                    }
                    Some(ms) => {
                        self.blank_frames = 0;
                        // Reset to ~0 after real progress -> restart.
                        if self.last_ms > RESTART_DROP_MS && ms + RESTART_DROP_MS < self.last_ms {
                            self.enter_running(f, ms);
                            return Some(SessionEvent::Restart);
                        }
                        // Frozen timer -> finish once it has been stable long enough, but
                        // only if the run actually got going. A brief freeze early on (or a
                        // low glitch value) shouldn't look "finished".
                        if ms == self.last_ms {
                            self.stable_frames += 1;
                            if self.stable_frames >= self.finish_frames
                                && self.peak_ms >= MIN_FINISH_MS
                            {
                                self.phase = Phase::Finished;
                                return Some(SessionEvent::Finish);
                            }
                            return Some(SessionEvent::Progress);
                        }
                        // Timer advancing normally.
                        self.last_ms = ms;
                        self.peak_ms = self.peak_ms.max(ms);
                        self.stable_frames = 0;
                        Some(SessionEvent::Progress)
                    }
                }
            }

            Phase::Finished => match cur {
                // The timer went blank (menus / loading / telemetry gone): the
                // finished run is truly over, so a small running timer after this
                // is a genuinely new run.
                None => {
                    self.saw_gap = true;
                    None
                }
                // A fresh run starting (timer running again from near zero). The
                // gap requirement keeps the result screen's still-ticking timer —
                // which resets to ~0 at the line — from opening a phantom session.
                Some(ms) if ms < FRESH_RUN_MS && self.saw_gap => {
                    self.enter_running(f, ms);
                    Some(SessionEvent::Start)
                }
                _ => None,
            },
        }
    }

    fn enter_running(&mut self, f: &Frame, ms: u32) {
        self.phase = Phase::Running;
        self.last_ms = ms;
        self.peak_ms = ms;
        self.stable_frames = 0;
        self.blank_frames = 0;
        self.baseline_last = f.last_laptime.clone();
        self.saw_gap = false;
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    fn frame(current: &str) -> Frame {
        frame_with_last(current, "-")
    }

    fn frame_with_last(current: &str, last: &str) -> Frame {
        use crate::model::SimStatus;
        Frame {
            status: SimStatus::Off,
            session_type: 0,
            current_lap: 0,
            end_session: false,
            current_laptime: current.into(),
            last_laptime: last.into(),
            best_laptime: "-".into(),
            current_time_ms: 0,
            last_time_ms: 0,
            best_time_ms: 0,
            completed_laps: 0,
            current_sector: 0,
            distance_m: 0.0,
            is_invalid: false,
            speed_kmh: 0.0,
            gear: 0,
            rpm: 0,
            driver: "D".into(),
            car: "C".into(),
            track: "T".into(),
            track_config: "".into(),
        }
    }

    #[test]
    fn start_progress_then_finish() {
        let mut m = SessionMachine::new(3);
        assert_eq!(m.observe(&frame("-")), None); // idle
        assert_eq!(m.observe(&frame("0:01.000")), Some(SessionEvent::Start));
        assert_eq!(m.observe(&frame("0:02.000")), Some(SessionEvent::Progress));
        assert_eq!(m.observe(&frame("0:03.000")), Some(SessionEvent::Progress));
        // Timer freezes at the finish; after enough stable frames -> Finish.
        assert_eq!(m.observe(&frame("4:05.722")), Some(SessionEvent::Progress));
        assert_eq!(m.observe(&frame("4:05.722")), Some(SessionEvent::Progress));
        assert_eq!(m.observe(&frame("4:05.722")), Some(SessionEvent::Progress));
        assert_eq!(m.observe(&frame("4:05.722")), Some(SessionEvent::Finish));
        // Frozen afterwards -> nothing.
        assert_eq!(m.observe(&frame("4:05.722")), None);
    }

    #[test]
    fn early_glitches_dont_finish_a_single_run() {
        let mut m = SessionMachine::new(3);
        assert_eq!(m.observe(&frame("0:02.000")), Some(SessionEvent::Start));
        // A freeze while still under the 10s floor is not a finish, however long.
        assert_eq!(m.observe(&frame("0:02.000")), Some(SessionEvent::Progress));
        assert_eq!(m.observe(&frame("0:02.000")), Some(SessionEvent::Progress));
        assert_eq!(m.observe(&frame("0:02.000")), Some(SessionEvent::Progress));
        assert_eq!(m.observe(&frame("0:02.000")), Some(SessionEvent::Progress));
        // A blank glitch frame is ignored, not a finish.
        assert_eq!(m.observe(&frame("-")), Some(SessionEvent::Progress));
        // Climb past the floor and run to a real, frozen finish.
        assert_eq!(m.observe(&frame("0:05.000")), Some(SessionEvent::Progress));
        assert_eq!(m.observe(&frame("4:15.036")), Some(SessionEvent::Progress));
        assert_eq!(m.observe(&frame("4:15.036")), Some(SessionEvent::Progress));
        assert_eq!(m.observe(&frame("4:15.036")), Some(SessionEvent::Progress));
        assert_eq!(m.observe(&frame("4:15.036")), Some(SessionEvent::Finish));
    }

    #[test]
    fn detects_restart() {
        let mut m = SessionMachine::new(3);
        m.observe(&frame("0:01.000")); // Start
        m.observe(&frame("1:00.000")); // Progress (running a while)
                                       // Stage restarted: timer jumps back to ~0.
        assert_eq!(m.observe(&frame("0:01.500")), Some(SessionEvent::Restart));
        // ...and continues as a normal new run.
        assert_eq!(m.observe(&frame("0:02.500")), Some(SessionEvent::Progress));
    }

    #[test]
    fn finish_line_crossing_beats_restart_heuristic() {
        let mut m = SessionMachine::new(3);
        assert_eq!(m.observe(&frame("0:01.000")), Some(SessionEvent::Start));
        assert_eq!(m.observe(&frame("2:00.000")), Some(SessionEvent::Progress));
        // Crossing the line: `last_laptime` gets the stage time while the live
        // timer resets and keeps ticking behind the result screen. Without the
        // last-time signal this drop would misread as a Restart and the runner
        // would abort the session that just earned a result.
        assert_eq!(
            m.observe(&frame_with_last("0:00.100", "2:00.128")),
            Some(SessionEvent::Finish)
        );
        // The result screen's still-ticking timer must not open a phantom run...
        assert_eq!(m.observe(&frame_with_last("0:01.000", "2:00.128")), None);
        assert_eq!(m.observe(&frame_with_last("0:02.000", "2:00.128")), None);
        // ...until a blank gap (menus / loading) separates it from a new run.
        assert_eq!(m.observe(&frame_with_last("-", "2:00.128")), None);
        assert_eq!(
            m.observe(&frame_with_last("0:01.500", "2:00.128")),
            Some(SessionEvent::Start)
        );
        // And the new run finishes normally on its own new last time.
        assert_eq!(
            m.observe(&frame_with_last("1:55.000", "2:00.128")),
            Some(SessionEvent::Progress)
        );
        assert_eq!(
            m.observe(&frame_with_last("0:00.050", "1:55.031")),
            Some(SessionEvent::Finish)
        );
    }

    #[test]
    fn stale_last_time_does_not_finish_a_new_run_early() {
        let mut m = SessionMachine::new(3);
        // A previous run's time sits in last_laptime when this run starts; it is
        // the baseline, not a finish signal.
        assert_eq!(
            m.observe(&frame_with_last("0:01.000", "2:00.128")),
            Some(SessionEvent::Start)
        );
        assert_eq!(
            m.observe(&frame_with_last("0:30.000", "2:00.128")),
            Some(SessionEvent::Progress)
        );
        assert_eq!(
            m.observe(&frame_with_last("0:31.000", "2:00.128")),
            Some(SessionEvent::Progress)
        );
    }

    #[test]
    fn finish_via_sustained_timer_clear() {
        let mut m = SessionMachine::new(3);
        m.observe(&frame("0:01.000")); // Start
        m.observe(&frame("2:00.000")); // Progress (past the 10s floor)
                                       // Back to menu: the timer clears. A single blank is ignored; a sustained
                                       // clear (finish_frames) after a real run is a finish.
        assert_eq!(m.observe(&frame("-")), Some(SessionEvent::Progress));
        assert_eq!(m.observe(&frame("-")), Some(SessionEvent::Progress));
        assert_eq!(m.observe(&frame("-")), Some(SessionEvent::Finish));
    }
}
