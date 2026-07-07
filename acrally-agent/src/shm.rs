//! Windows shared-memory telemetry source for **Assetto Corsa Rally**.
//!
//! Compiled only on Windows (the game's platform) behind the `shm` feature.
//!
//! IMPORTANT — which game / which layout:
//! AC Rally is a *different* game from AC EVO and, like AC1 / ACC, publishes its
//! telemetry under the classic segment names `Local\acpmf_physics` / `_graphics`
//! / `_static` (NOT EVO's `acevo_pmf_*`). The struct layouts below are the
//! classic Assetto Corsa layouts; we deliberately read only the *early, stable*
//! fields (speed/gear/rpm, status/session, the wchar lap-time strings, and the
//! track/car/driver text), because those offsets are the ones least likely to
//! have shifted in Rally's variant. Deeper fields (completed-lap count, lap
//! validity) are intentionally omitted until the base layout is confirmed on the
//! real game — extend the structs once speed/times are verified to read cleanly.
//!
//! The segments are only published while a session is loaded (car on track), not
//! in menus — expect `open` to fail until you're actually driving.

use win_shared_memory::SharedMemoryLink;

use crate::logfile::agent_log;
use crate::model::{scan_report, utf16_to_string, Frame, SimStatus};
use crate::telemetry::TelemetrySource;

const PHYSICS: &str = "Local\\acpmf_physics";
const GRAPHICS: &str = "Local\\acpmf_graphics";
const STATIC: &str = "Local\\acpmf_static";

/// Leading fields of the classic AC physics page (rest of the struct omitted —
/// we only read up to `speed_kmh`, which is enough for a liveness signal).
#[repr(C)]
#[derive(Clone, Copy)]
struct AcPhysics {
    packet_id: i32,
    gas: f32,
    brake: f32,
    fuel: f32,
    gear: i32,
    rpms: i32,
    steer_angle: f32,
    speed_kmh: f32,
}

/// Classic AC graphics page, extended through the integer timing/lap fields.
/// Strings are `wchar_t[15]` (UTF-16), e.g. "1:23.456". The fields past the
/// string block are at deeper offsets and need on-Rally verification (watch the
/// heartbeat at a stage finish to see which one fires).
#[repr(C)]
#[derive(Clone, Copy)]
struct AcGraphics {
    packet_id: i32, // 0
    /// AC_STATUS: 0 off, 1 replay, 2 live, 3 pause.
    status: i32, // 4
    /// AC_SESSION_TYPE: -1 unknown, 0 practice, 1 qualify, 2 race, 3 hotlap, ...
    session: i32, // 8
    current_time: [u16; 15], // 12
    last_time: [u16; 15], // 42
    best_time: [u16; 15], // 72
    split: [u16; 15], // 102
    completed_laps: i32, // 132
    position: i32,  // 136
    i_current_time: i32, // 140
    i_last_time: i32, // 144
    i_best_time: i32, // 148
    session_time_left: f32, // 152
    distance_traveled: f32, // 156
    is_in_pit: i32, // 160
    current_sector_index: i32, // 164
    last_sector_time: i32, // 168
    number_of_laps: i32, // 172
}

/// Leading fields of the classic AC static page (up to the driver name).
#[repr(C)]
#[derive(Clone, Copy)]
struct AcStatic {
    sm_version: [u16; 15],
    ac_version: [u16; 15],
    number_of_sessions: i32,
    num_cars: i32,
    car_model: [u16; 33],
    track: [u16; 33],
    player_name: [u16; 33],
    player_surname: [u16; 33],
    player_nick: [u16; 33],
}

struct Links {
    physics: SharedMemoryLink<AcPhysics>,
    graphics: SharedMemoryLink<AcGraphics>,
    statics: SharedMemoryLink<AcStatic>,
}

/// Consecutive polls with unmoving packet ids before the mapping counts as
/// stale (~1.5s at the 10 Hz poll rate — matches the finish debounce).
///
/// Staleness matters because our own open handle keeps the section object
/// alive after the game stops publishing (result screen, menus, game exit):
/// reads keep returning the last-written frame forever, so without this check
/// `poll` would never report "no telemetry" again after the first open — the
/// UI shows a phantom "driving" and the blank-frame finish path never runs.
const STALE_POLLS: u32 = 15;

pub struct ShmSource {
    links: Option<Links>,
    /// Last observed (physics, graphics) packet ids, for staleness detection.
    last_packets: (i32, i32),
    stale_polls: u32,
}

impl ShmSource {
    pub fn new() -> Self {
        ShmSource {
            links: None,
            last_packets: (0, 0),
            stale_polls: 0,
        }
    }

    /// Open all three segments, or report failure (game not running / not yet in
    /// a session). All-or-nothing so we never read a half-open set.
    fn ensure_open(&mut self) -> bool {
        if self.links.is_some() {
            return true;
        }
        match (
            SharedMemoryLink::<AcPhysics>::open(PHYSICS),
            SharedMemoryLink::<AcGraphics>::open(GRAPHICS),
            SharedMemoryLink::<AcStatic>::open(STATIC),
        ) {
            (Ok(physics), Ok(graphics), Ok(statics)) => {
                agent_log!("shared memory opened — game telemetry available");
                self.links = Some(Links {
                    physics,
                    graphics,
                    statics,
                });
                true
            }
            _ => false,
        }
    }
}

impl TelemetrySource for ShmSource {
    fn poll(&mut self) -> Option<Frame> {
        if !self.ensure_open() {
            return None;
        }

        // Read everything unconditionally so the heartbeat reflects what's
        // actually in memory regardless of the (possibly misread) status field.
        let links = self.links.as_ref().unwrap();
        // Safety: the segments stay mapped for the lifetime of `links`; the sim
        // writes packets atomically, which is fine for telemetry reads.
        let p = unsafe { links.physics.get() };
        let g = unsafe { links.graphics.get() };
        let s = unsafe { links.statics.get() };

        // The sim bumps a packet id on every write (physics while live, graphics
        // even while paused). Both frozen for a sustained stretch means nobody is
        // writing anymore — report "no telemetry" instead of the stale frame.
        let packets = (p.packet_id, g.packet_id);
        if packets == self.last_packets {
            self.stale_polls = self.stale_polls.saturating_add(1);
            if self.stale_polls >= STALE_POLLS {
                // Log the moment telemetry goes stale (once, on the threshold
                // crossing) — whether a gap was seen between two runs is exactly
                // what a missed-run investigation needs to know.
                if self.stale_polls == STALE_POLLS {
                    agent_log!("telemetry stale — game stopped publishing (menus/result screen/exit)");
                }
                return None;
            }
        } else {
            if self.stale_polls >= STALE_POLLS {
                agent_log!("telemetry resumed — game is publishing again");
            }
            self.last_packets = packets;
            self.stale_polls = 0;
        }

        let driver = format!(
            "{} {}",
            utf16_to_string(&s.player_name),
            utf16_to_string(&s.player_surname)
        )
        .trim()
        .to_string();

        Some(Frame {
            status: SimStatus::from_raw(g.status),
            session_type: g.session,
            current_lap: 0,
            end_session: false,
            current_laptime: utf16_to_string(&g.current_time),
            last_laptime: utf16_to_string(&g.last_time),
            best_laptime: utf16_to_string(&g.best_time),
            current_time_ms: g.i_current_time,
            last_time_ms: g.i_last_time,
            best_time_ms: g.i_best_time,
            completed_laps: g.completed_laps,
            current_sector: g.current_sector_index,
            distance_m: g.distance_traveled,
            // Lap validity lives deep in the struct; defaults to valid until the
            // base layout is confirmed on the real game.
            is_invalid: false,
            speed_kmh: p.speed_kmh,
            // AC encodes gear as 0=reverse, 1=neutral, 2=1st; shift down so
            // neutral reads as 0 in the heartbeat.
            gear: p.gear - 1,
            rpm: p.rpms,
            driver,
            car: utf16_to_string(&s.car_model),
            track: utf16_to_string(&s.track),
            track_config: String::new(),
        })
    }

    fn name(&self) -> &'static str {
        "shared-memory (acpmf)"
    }
}

/// Read a whole segment into a byte vector, trying progressively smaller sizes
/// (the section size is unknown and `open` fails if we ask for more than exists).
fn read_segment(name: &str) -> Option<Vec<u8>> {
    macro_rules! try_size {
        ($n:expr) => {
            if let Ok(link) = SharedMemoryLink::<[u8; $n]>::open(name) {
                let bytes = unsafe { link.get() };
                return Some(bytes.to_vec());
            }
        };
    }
    try_size!(4096);
    try_size!(3072);
    try_size!(2048);
    try_size!(1536);
    try_size!(1024);
    try_size!(768);
    try_size!(512);
    try_size!(256);
    None
}

/// Diagnostic: scan all three segments for every string and time-like value.
/// Triggered by `ACRALLY_SCAN=1` (repeats every couple of seconds). Drive to the
/// finish, stop with the final time on screen, and read the output — the
/// penalized finish time and penalty will appear as a string or int with an
/// offset we can then read directly.
pub fn scan() {
    for name in [PHYSICS, GRAPHICS, STATIC] {
        match read_segment(name) {
            Some(bytes) => {
                println!("===== scan {name} ({} bytes) =====", bytes.len());
                for line in scan_report(&bytes) {
                    println!("{line}");
                }
            }
            None => println!("===== scan {name}: could not open — are you on track? ====="),
        }
    }
}

/// Diagnostic: dump the first bytes of each segment as hex + a few interpreted
/// candidate values, so field offsets can be verified/reverse-engineered against
/// the real game. Triggered by `ACRALLY_DUMP=1`. Run this while actively driving.
pub fn dump_hex() {
    for name in [PHYSICS, GRAPHICS, STATIC] {
        match SharedMemoryLink::<[u8; 256]>::open(name) {
            Ok(link) => {
                let bytes = unsafe { link.get() };
                println!("=== {name} (first 96 bytes) ===");
                for (row, chunk) in bytes[..96].chunks(16).enumerate() {
                    let hex: Vec<String> = chunk.iter().map(|b| format!("{b:02x}")).collect();
                    let ascii: String = chunk
                        .iter()
                        .map(|&b| {
                            if (0x20..0x7f).contains(&b) {
                                b as char
                            } else {
                                '.'
                            }
                        })
                        .collect();
                    println!("  {:3}: {}  {ascii}", row * 16, hex.join(" "));
                }
                // A few interpreted candidates to speed up offset-spotting.
                let i32_at = |off: usize| {
                    i32::from_le_bytes([bytes[off], bytes[off + 1], bytes[off + 2], bytes[off + 3]])
                };
                let f32_at = |off: usize| {
                    f32::from_le_bytes([bytes[off], bytes[off + 1], bytes[off + 2], bytes[off + 3]])
                };
                print!(
                    "  int@0={} int@4={} int@8={}",
                    i32_at(0),
                    i32_at(4),
                    i32_at(8)
                );
                println!(
                    "  float@16={:.2} float@20={:.2} float@24={:.2} float@28={:.2}",
                    f32_at(16),
                    f32_at(20),
                    f32_at(24),
                    f32_at(28)
                );
            }
            Err(e) => println!("=== {name}: could not open ({e:?}) — are you on track? ==="),
        }
    }
}
