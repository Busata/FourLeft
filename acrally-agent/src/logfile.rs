//! Persistent agent log.
//!
//! The distributed build is a windowless GUI app (`windows_subsystem = "windows"`),
//! so the console diagnostics that explain why a run was or wasn't submitted are
//! invisible in normal use — a missed run leaves no trace to debug afterwards.
//! Every pipeline event is therefore also appended, timestamped (UTC), to
//! `agent.log` in the per-user config dir. An oversized log is rotated to
//! `agent.log.old` at startup, so it never grows unbounded.

use std::fs::{File, OpenOptions};
use std::io::Write;
use std::path::PathBuf;
use std::sync::{Mutex, OnceLock};
use std::time::{SystemTime, UNIX_EPOCH};

/// Rotate (`agent.log` -> `agent.log.old`) when the log exceeds this at startup.
const MAX_LOG_BYTES: u64 = 1_000_000;

/// Log a line: echo to stdout (visible in console/headless runs) and append it,
/// timestamped, to the log file (the only record in the windowless GUI build).
macro_rules! agent_log {
    ($($arg:tt)*) => {
        crate::logfile::write_line(format!($($arg)*))
    };
}
pub(crate) use agent_log;

pub fn path() -> PathBuf {
    crate::config::config_dir().join("agent.log")
}

static LOG: OnceLock<Mutex<Option<File>>> = OnceLock::new();

pub fn write_line(msg: String) {
    println!("{msg}");
    let file = LOG.get_or_init(|| Mutex::new(open()));
    if let Ok(mut guard) = file.lock() {
        if let Some(f) = guard.as_mut() {
            let _ = writeln!(f, "{} {msg}", timestamp_utc());
        }
    }
}

/// Open the log for appending, creating the config dir if needed and rotating
/// an oversized file first. `None` (silently) when the filesystem refuses —
/// the agent must never fail because its log can't be written.
fn open() -> Option<File> {
    let path = path();
    if let Some(dir) = path.parent() {
        let _ = std::fs::create_dir_all(dir);
    }
    if std::fs::metadata(&path).map_or(false, |m| m.len() > MAX_LOG_BYTES) {
        let _ = std::fs::rename(&path, path.with_extension("log.old"));
    }
    OpenOptions::new().create(true).append(true).open(&path).ok()
}

/// `YYYY-MM-DDTHH:MM:SSZ` from the system clock, without a date-time dependency.
fn timestamp_utc() -> String {
    let secs = SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .map(|d| d.as_secs())
        .unwrap_or(0);
    let (days, rem) = (secs / 86_400, secs % 86_400);
    let (y, mo, d) = civil_from_days(days as i64);
    format!(
        "{y:04}-{mo:02}-{d:02}T{:02}:{:02}:{:02}Z",
        rem / 3600,
        (rem % 3600) / 60,
        rem % 60
    )
}

/// Days-since-1970-01-01 -> (year, month, day). Howard Hinnant's civil-from-days.
fn civil_from_days(z: i64) -> (i64, u32, u32) {
    let z = z + 719_468;
    let era = z.div_euclid(146_097);
    let doe = z.rem_euclid(146_097);
    let yoe = (doe - doe / 1_460 + doe / 36_524 - doe / 146_096) / 365;
    let doy = doe - (365 * yoe + yoe / 4 - yoe / 100);
    let mp = (5 * doy + 2) / 153;
    let d = (doy - (153 * mp + 2) / 5 + 1) as u32;
    let m = (if mp < 10 { mp + 3 } else { mp - 9 }) as u32;
    (yoe + era * 400 + i64::from(m <= 2), m, d)
}

#[cfg(test)]
mod tests {
    use super::civil_from_days;

    #[test]
    fn civil_dates() {
        assert_eq!(civil_from_days(0), (1970, 1, 1));
        assert_eq!(civil_from_days(19_723), (2024, 1, 1)); // leap year
        assert_eq!(civil_from_days(19_723 + 59), (2024, 2, 29));
        assert_eq!(civil_from_days(20_641), (2026, 7, 7));
    }
}
