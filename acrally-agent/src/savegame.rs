//! Assetto Corsa Rally save-game parsing.
//!
//! The game writes `PlayerDataSaveSlot.sav` (an Unreal "GVAS" binary save) after
//! each completed stage. It holds the authoritative, penalty-inclusive stage
//! records — which shared memory does NOT expose. Rather than fully parse GVAS
//! (whose header is version-fragile), we anchor on each record's timestamp and
//! read fields at fixed offsets around it, a layout validated against real saves:
//!
//!   anchor  = i64 .NET-ticks timestamp (recent years ~6.0e17..7.0e17)
//!   raw     = f32 seconds at  anchor - 20   (driving time, no penalty)
//!   penalty = f32 seconds at  anchor - 12
//!   total   = raw + penalty                 (the official result)
//!   stage, car = the two content strings immediately preceding
//!
//! Slot semantics (observed 2026-07-07 against live play): the save keeps a
//! bounded history (~10 entries) with ONE record per *event entry*. The
//! timestamp is stamped in UTC when the player enters the event — NOT when a
//! run finishes — and each completed run in that event OVERWRITES the slot's
//! times while keeping the timestamp. So the newest record is the record with
//! the largest timestamp, but its times can change without the timestamp
//! moving; see [`StageRecord::content_key`].

use std::path::{Path, PathBuf};

/// One completed stage record from the save file.
#[derive(Debug, Clone, PartialEq)]
pub struct StageRecord {
    /// .NET ticks (100 ns since 0001-01-01, UTC), stamped at *event entry* —
    /// sortable; newest = largest. Two runs in the same event share this value.
    pub timestamp_ticks: i64,
    pub stage: String,
    pub car: String,
    pub raw_ms: u32,
    pub penalty_ms: u32,
    pub total_ms: u32,
}

/// A record's identity for novelty detection: (timestamp, raw, penalty).
/// The timestamp alone can't distinguish two runs of the same event — the game
/// overwrites the event's slot with each completed run's times — so the times
/// are part of the identity.
pub type RecordKey = (i64, u32, u32);

impl StageRecord {
    pub fn content_key(&self) -> RecordKey {
        (self.timestamp_ticks, self.raw_ms, self.penalty_ms)
    }
}

/// .NET ticks range for plausible recent save dates (~2000..2200).
const TICKS_MIN: i64 = 600_000_000_000_000_000;
const TICKS_MAX: i64 = 700_000_000_000_000_000;

fn read_f32(b: &[u8], o: usize) -> Option<f32> {
    b.get(o..o + 4)
        .map(|s| f32::from_le_bytes([s[0], s[1], s[2], s[3]]))
}

fn read_i64(b: &[u8], o: usize) -> Option<i64> {
    b.get(o..o + 8)
        .map(|s| i64::from_le_bytes(s.try_into().unwrap()))
}

/// Read an Unreal FString (i32 length prefix + Latin-1 bytes incl. trailing NUL)
/// at `o`. Returns the string and the offset just past it. ASCII/Latin-1 only —
/// the fields we care about (stage IDs, car names) are ASCII.
fn read_fstring(b: &[u8], o: usize) -> Option<(String, usize)> {
    let len = i32::from_le_bytes(b.get(o..o + 4)?.try_into().ok()?);
    if len <= 1 || len > 128 {
        return None;
    }
    let start = o + 4;
    let end = start + len as usize - 1; // drop trailing NUL
    let raw = b.get(start..end)?;
    if raw.iter().all(|&c| (0x20..0x7f).contains(&c)) {
        Some((
            raw.iter().map(|&c| c as char).collect(),
            o + 4 + len as usize,
        ))
    } else {
        None
    }
}

/// A content string with its position and whether it's a stage header.
struct Content {
    off: usize,
    /// Offset just past the string (incl. its NUL), for anchor-adjacency checks.
    end: usize,
    val: String,
    is_stage: bool,
}

/// Collect content strings (skipping UE property-path strings starting with '/')
/// and flag stage headers. The save nests multiple car records under one stage
/// header as `[stage][car][block][car][block]…`: a stage name sits immediately
/// before its car (tiny gap), whereas a car is followed by a ~70-byte data block.
/// Flagging by that gap lets us attribute the correct stage to a record no matter
/// how many car records are nested under it (distance alone can't).
fn collect_content(b: &[u8]) -> Vec<Content> {
    let mut items: Vec<(usize, usize, String)> = Vec::new(); // (off, end, val)
    let mut o = 0;
    while o + 4 <= b.len() {
        if let Some((s, next)) = read_fstring(b, o) {
            if !s.starts_with('/') && s.len() >= 3 {
                items.push((o, next, s));
            }
            o = next;
        } else {
            o += 1;
        }
    }
    (0..items.len())
        .map(|i| {
            let is_stage = i + 1 < items.len() && items[i + 1].0 - items[i].1 < 30;
            Content {
                off: items[i].0,
                end: items[i].1,
                val: items[i].2.clone(),
                is_stage,
            }
        })
        .collect()
}

/// Parse all stage records from raw save bytes.
pub fn parse_records(bytes: &[u8]) -> Vec<StageRecord> {
    let content = collect_content(bytes);
    let mut records = Vec::new();
    let mut o = 0usize;
    while o + 8 <= bytes.len() {
        let ticks = match read_i64(bytes, o) {
            Some(v) => v,
            None => break,
        };
        if !(TICKS_MIN..TICKS_MAX).contains(&ticks) {
            o += 1;
            continue;
        }
        // Candidate anchor. Validate the raw-time float sits where expected.
        let raw = read_f32(bytes, o.wrapping_sub(20)).unwrap_or(0.0);
        if !(5.0..3600.0).contains(&raw) {
            o += 1;
            continue;
        }
        // The penalty must also be a plausible seconds value. The save's car-usage
        // section stores `[car name][i64 local-time timestamp]` pairs whose string
        // bytes can decode to an in-range "raw" — but never to a sane penalty
        // (observed 2026-07-08: raw 8.0, penalty ~7e28, and a timestamp that
        // outranked every real record because it advances with each save write).
        let penalty = match read_f32(bytes, o - 12) {
            Some(p) if p.is_finite() && (-1.0..3600.0).contains(&p) => p.max(0.0),
            _ => {
                o += 1;
                continue;
            }
        };
        // A real anchor sits ~64+ bytes into a record's numeric block; an i64
        // whose time floats would overlap the preceding string is one of those
        // car-usage timestamps, not a record.
        let prev_end = content.iter().filter(|c| c.end <= o).last().map_or(0, |c| c.end);
        if o - prev_end < 32 {
            o += 1;
            continue;
        }

        // Stage = the last stage header before this record; car = the last
        // non-header string before the numeric block (records repeat the car).
        let stage = content
            .iter()
            .filter(|c| c.off < o && c.is_stage)
            .last()
            .map(|c| c.val.clone())
            .unwrap_or_default();
        let car = content
            .iter()
            .filter(|c| c.off < o.saturating_sub(20) && !c.is_stage)
            .last()
            .map(|c| c.val.clone())
            .unwrap_or_default();

        let raw_ms = (raw * 1000.0).round() as u32;
        let penalty_ms = (penalty * 1000.0).round() as u32;
        records.push(StageRecord {
            timestamp_ticks: ticks,
            stage,
            car,
            raw_ms,
            penalty_ms,
            total_ms: raw_ms + penalty_ms,
        });
        o += 8;
    }
    records
}

/// The most recently completed stage (largest timestamp), if any.
pub fn newest_record(bytes: &[u8]) -> Option<StageRecord> {
    parse_records(bytes)
        .into_iter()
        .max_by_key(|r| r.timestamp_ticks)
}

/// Auto-detect the save file. Checks (in order): an explicit override, then the
/// standard UE location under `%LOCALAPPDATA%`, then a `%USERPROFILE%` fallback.
pub fn locate_save(override_path: Option<&str>) -> Option<PathBuf> {
    const REL: &str = "acr/Saved/SaveGames/PlayerDataSaveSlot.sav";

    if let Some(p) = override_path {
        let p = PathBuf::from(p);
        if p.is_file() {
            return Some(p);
        }
    }

    let mut roots: Vec<PathBuf> = Vec::new();
    if let Ok(local) = std::env::var("LOCALAPPDATA") {
        roots.push(PathBuf::from(local));
    }
    if let Ok(profile) = std::env::var("USERPROFILE") {
        roots.push(Path::new(&profile).join("AppData").join("Local"));
    }

    roots.into_iter().map(|r| r.join(REL)).find(|p| p.is_file())
}

#[cfg(test)]
mod tests {
    use super::*;

    const SAVE: &[u8] = include_bytes!("../tests/fixtures/PlayerDataSaveSlot.sav");

    #[test]
    fn newest_record_matches_reality() {
        let r = newest_record(SAVE).expect("should find records");
        // Most recent run in the fixture: Hyundai on Weles S4 Reverse, raw
        // 3:58.874 (238874 ms) + 10s penalty = 4:08.874 (248874 ms). Three car
        // entries are nested between this record and the stage header — the case
        // that broke distance-based stage lookup.
        assert_eq!(r.car, "HyundaiI20NRally2");
        assert_eq!(r.stage, "WelesS4HafrenSouthFullReverse");
        assert_eq!(r.raw_ms, 238_874);
        assert_eq!(r.penalty_ms, 10_000);
        assert_eq!(r.total_ms, 248_874);
    }

    #[test]
    fn finds_the_penalised_runs() {
        let recs = parse_records(SAVE);
        let totals: Vec<u32> = recs.iter().map(|r| r.total_ms).collect();
        assert!(totals.contains(&443_653), "7:23.653 total present");
        assert!(totals.contains(&343_722), "5:43.722 total present");
        assert!(totals.contains(&290_937), "4:50.937 total present");
    }

    // 2026-07-08 save, captured live: alongside 10 real records it has a
    // car-usage entry — `[AlfaRomeoGTA1300][i64 local-time timestamp]` — whose
    // string bytes decoded to an in-range raw (8.0s) with a ~7e28 "penalty".
    // Its timestamp advanced on every save write and outranked every real
    // record, so the agent kept confirming a garbage result and real runs were
    // never posted.
    const SAVE_CAR_USAGE: &[u8] =
        include_bytes!("../tests/fixtures/PlayerDataSaveSlot-carusage.sav");

    #[test]
    fn ignores_car_usage_timestamps() {
        let recs = parse_records(SAVE_CAR_USAGE);
        assert_eq!(recs.len(), 10, "exactly the real records, no garbage anchor");
        for r in &recs {
            assert!(r.penalty_ms < 3_600_000, "sane penalty: {:?}", r);
            assert!((5_000..3_600_000).contains(&r.raw_ms), "sane raw: {:?}", r);
        }
        let newest = newest_record(SAVE_CAR_USAGE).expect("should find records");
        assert_eq!(newest.timestamp_ticks, 639_191_101_215_490_000);
        assert_eq!(newest.stage, "AlsaceS2MunsterShort2Reverse");
        assert_eq!(newest.car, "AlfaRomeoGTA1300");
        assert_eq!(newest.total_ms, 206_954);
    }

    #[test]
    fn stage_never_equals_car() {
        // A record's stage must not be mis-read as the car name.
        for r in parse_records(SAVE) {
            if !r.stage.is_empty() {
                assert_ne!(r.stage, r.car, "stage should not duplicate the car");
            }
        }
    }
}
