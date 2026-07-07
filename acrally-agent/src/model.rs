//! Data shared across the agent: the normalized telemetry `Frame` and the
//! server payload types that get POSTed to the club backend.
//!
//! Some fields/helpers here are consumed only by the Windows `shm` layer, so on
//! a non-Windows (mock) build they read as dead — allow it module-wide.
#![allow(dead_code)]

use serde::{Deserialize, Serialize};

/// Operational state of the simulator (mirrors AC's `ACEVO_STATUS`).
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum SimStatus {
    Off,
    Replay,
    Live,
    Pause,
    Unknown(i32),
}

impl SimStatus {
    pub fn from_raw(v: i32) -> Self {
        match v {
            0 => SimStatus::Off,
            1 => SimStatus::Replay,
            2 => SimStatus::Live,
            3 => SimStatus::Pause,
            other => SimStatus::Unknown(other),
        }
    }
}

/// A single normalized telemetry sample, decoupled from the raw shared-memory
/// layout so the detector and the rest of the pipeline never touch platform
/// specifics.
#[derive(Debug, Clone)]
pub struct Frame {
    pub status: SimStatus,
    /// Session type from the static page (-1 unknown, 0 time attack, 2 hot stint, ...).
    pub session_type: i32,
    /// Lap number currently being driven.
    pub current_lap: i32,
    /// True when the game signals the session is ending (checkered / stage finish).
    pub end_session: bool,
    /// Current (in-progress) lap time, formatted. Not used for detection, but a
    /// handy liveness signal — it ticks up while you drive.
    pub current_laptime: String,
    /// Last *completed* lap time, as the game formats it (e.g. "1:23.456").
    pub last_laptime: String,
    /// Personal-best lap time this session, formatted.
    pub best_laptime: String,
    /// Integer millisecond time fields (graphics page). On rally stages these
    /// often carry the timing when the wchar strings above stay empty.
    pub current_time_ms: i32,
    pub last_time_ms: i32,
    pub best_time_ms: i32,
    /// Completed-lap counter (a rally stage finish may tick this 0 -> 1).
    pub completed_laps: i32,
    /// Current sector index and distance travelled — stage-progress signals.
    pub current_sector: i32,
    pub distance_m: f32,
    /// True while the in-progress lap is invalidated (track limits, etc.).
    pub is_invalid: bool,
    /// Live vehicle speed (km/h) from the physics page — a strong liveness signal.
    pub speed_kmh: f32,
    /// Current gear (physics page).
    pub gear: i32,
    /// Engine RPM (graphics page).
    pub rpm: i32,
    pub driver: String,
    pub car: String,
    pub track: String,
    pub track_config: String,
}

impl Frame {
    /// A synthetic "no telemetry" frame whose timer reads blank. Fed to the session machine when the
    /// live source stops publishing — AC Rally drops its shared memory the instant a stage finishes
    /// and the result screen appears, so without this the frozen/blank finish path never gets a frame
    /// and the run never finalises (the save is never read). The blank timer drives the existing
    /// debounced blank-finish path; `finish()` uses the session's stored driver, not this frame.
    pub fn blank() -> Self {
        Frame {
            status: SimStatus::Off,
            session_type: -1,
            current_lap: 0,
            end_session: false,
            current_laptime: String::new(),
            last_laptime: String::new(),
            best_laptime: String::new(),
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
            driver: String::new(),
            car: String::new(),
            track: String::new(),
            track_config: String::new(),
        }
    }
}

/// Body of `POST /sessions` — opens a live session when driving starts.
#[derive(Debug, Clone, Serialize)]
pub struct SessionStart {
    pub driver: String,
    pub car: String,
    pub stage: String,
    pub track: String,
    pub started_at_ms: u128,
    pub agent_version: String,
}

/// Body of `POST /sessions/{id}/heartbeat` — live telemetry while driving.
#[derive(Debug, Clone, Serialize)]
pub struct Heartbeat {
    /// Current (in-progress) stage time in ms, if the timer is running.
    #[serde(skip_serializing_if = "Option::is_none")]
    pub current_ms: Option<u64>,
    pub speed_kmh: f32,
    pub gear: i32,
    pub rpm: i32,
    pub distance_m: f32,
}

/// Body of `POST /sessions/{id}/result` — the authoritative penalised result,
/// read from the save file when the stage completes.
#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ResultPayload {
    pub stage: String,
    pub car: String,
    pub driver: String,
    pub raw_ms: u32,
    pub penalty_ms: u32,
    pub total_ms: u32,
    /// Save-file timestamp (.NET ticks) — stable id for de-duping a result.
    pub timestamp_ticks: i64,
    pub agent_version: String,
}

/// Parse a formatted lap time into milliseconds.
///
/// Accepts `SS.mmm`, `M:SS.mmm`, `MM:SS.mmm`, and `H:MM:SS.mmm`.
/// Returns `None` for placeholders / empty / zero values.
pub fn parse_laptime_ms(s: &str) -> Option<u64> {
    let s = s.trim();
    if s.is_empty() || s.contains('-') {
        return None;
    }

    // Split into colon-separated components; last component carries the fraction.
    let parts: Vec<&str> = s.split(':').collect();
    if parts.is_empty() || parts.len() > 3 {
        return None;
    }

    let mut total_ms: u64 = 0;
    for (i, part) in parts.iter().enumerate() {
        let is_last = i == parts.len() - 1;
        if is_last {
            // seconds.milliseconds
            let (secs, millis) = match part.split_once('.') {
                Some((sec, frac)) => {
                    // Normalize fraction to exactly 3 digits.
                    let frac = frac.trim();
                    let mut frac = frac.to_string();
                    while frac.len() < 3 {
                        frac.push('0');
                    }
                    frac.truncate(3);
                    (sec.parse::<u64>().ok()?, frac.parse::<u64>().ok()?)
                }
                None => (part.parse::<u64>().ok()?, 0),
            };
            total_ms += secs * 1000 + millis;
        } else {
            let unit: u64 = part.parse().ok()?;
            // First-of-two is minutes; first-of-three is hours, second is minutes.
            let scale = if parts.len() == 3 && i == 0 {
                3_600_000
            } else {
                60_000
            };
            total_ms += unit * scale;
        }
    }

    if total_ms == 0 {
        None
    } else {
        Some(total_ms)
    }
}

/// True when the string looks like a real, completed lap time (not a placeholder).
pub fn is_real_laptime(s: &str) -> bool {
    parse_laptime_ms(s).is_some()
}

/// Format a millisecond duration as `M:SS.mmm`.
pub fn fmt_ms(ms: i32) -> String {
    let ms = ms.max(0);
    format!("{}:{:02}.{:03}", ms / 60_000, (ms % 60_000) / 1000, ms % 1000)
}

/// Diagnostic: scan a raw shared-memory segment and report everything that looks
/// like a lap/stage time or penalty, with byte offsets. Used to locate Rally's
/// finish-time and penalty fields, which aren't at the classic AC offsets.
///
/// Reports three things:
///  - UTF-16 (`wchar_t`) strings — this is where the game's formatted times live;
///  - i32 values in a plausible time range (1s..20min), formatted as `M:SS.mmm`;
///  - f32 values in a plausible penalty range (0.5..600s) — noisier, but a ~20s
///    penalty will show up here if it's stored as float seconds.
pub fn scan_report(bytes: &[u8]) -> Vec<String> {
    let mut out = Vec::new();

    out.push("  -- utf16 strings (>=4 chars) --".to_string());
    let mut i = 0;
    while i + 1 < bytes.len() {
        let mut j = i;
        let mut s = String::new();
        while j + 1 < bytes.len() {
            let c = u16::from_le_bytes([bytes[j], bytes[j + 1]]);
            if (0x20..0x7f).contains(&c) {
                s.push(c as u8 as char);
                j += 2;
            } else {
                break;
            }
        }
        if s.chars().count() >= 4 {
            out.push(format!("    @{i:>4}: \"{s}\""));
            i = j + 2;
        } else {
            i += 2;
        }
    }

    out.push("  -- int32 time-like (1000..1200000 ms) --".to_string());
    let mut k = 0;
    while k + 4 <= bytes.len() {
        let v = i32::from_le_bytes([bytes[k], bytes[k + 1], bytes[k + 2], bytes[k + 3]]);
        if (1000..=1_200_000).contains(&v) {
            out.push(format!("    @{k:>4}: {v} = {}", fmt_ms(v)));
        }
        k += 4;
    }

    out.push("  -- int32 small (5..3600, penalty seconds?) --".to_string());
    let mut p = 0;
    while p + 4 <= bytes.len() {
        let v = i32::from_le_bytes([bytes[p], bytes[p + 1], bytes[p + 2], bytes[p + 3]]);
        if (5..=3600).contains(&v) {
            out.push(format!("    @{p:>4}: {v}"));
        }
        p += 4;
    }

    out.push("  -- float32 penalty-like (0.5..600 s) --".to_string());
    let mut m = 0;
    while m + 4 <= bytes.len() {
        let v = f32::from_le_bytes([bytes[m], bytes[m + 1], bytes[m + 2], bytes[m + 3]]);
        if v.is_finite() && (0.5..=600.0).contains(&v) {
            out.push(format!("    @{m:>4}: {v:.3}"));
        }
        m += 4;
    }

    out
}

/// Decode a fixed-size UTF-16 (`wchar_t`) buffer up to the first NUL into a
/// trimmed `String`. Classic Assetto Corsa shared memory stores its text fields
/// this way (unlike the EVO layout, which uses byte chars).
pub fn utf16_to_string(buf: &[u16]) -> String {
    let end = buf.iter().position(|&c| c == 0).unwrap_or(buf.len());
    String::from_utf16_lossy(&buf[..end]).trim().to_string()
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn parses_common_formats() {
        assert_eq!(parse_laptime_ms("1:23.456"), Some(83_456));
        assert_eq!(parse_laptime_ms("01:23.456"), Some(83_456));
        assert_eq!(parse_laptime_ms("23.456"), Some(23_456));
        assert_eq!(parse_laptime_ms("1:02:03.000"), Some(3_723_000));
        assert_eq!(parse_laptime_ms("59.9"), Some(59_900));
    }

    #[test]
    fn rejects_placeholders() {
        assert_eq!(parse_laptime_ms(""), None);
        assert_eq!(parse_laptime_ms("--:--"), None);
        assert_eq!(parse_laptime_ms("0:00.000"), None);
        assert!(!is_real_laptime("-:--.---"));
        assert!(is_real_laptime("2:58.113"));
    }

    #[test]
    fn scan_finds_strings_and_times() {
        let mut bytes = vec![0u8; 64];
        // UTF-16LE "4:50.937" at offset 0.
        for (n, c) in "4:50.937".encode_utf16().enumerate() {
            bytes[n * 2..n * 2 + 2].copy_from_slice(&c.to_le_bytes());
        }
        // i32 290937 ms at offset 40.
        bytes[40..44].copy_from_slice(&290_937i32.to_le_bytes());
        let report = scan_report(&bytes).join("\n");
        assert!(report.contains("\"4:50.937\""), "should surface the wchar time");
        assert!(report.contains("290937 = 4:50.937"), "should surface the int time");
    }

    #[test]
    fn decodes_utf16() {
        // "1:23.456" then NUL, in a 15-wide buffer, with trailing garbage.
        let mut buf = [0u16; 15];
        for (i, c) in "1:23.456".encode_utf16().enumerate() {
            buf[i] = c;
        }
        buf[9] = 0x0041; // garbage after the NUL at index 8 — must be ignored
        assert_eq!(utf16_to_string(&buf), "1:23.456");
        assert_eq!(utf16_to_string(&[0u16; 15]), "");
    }
}
