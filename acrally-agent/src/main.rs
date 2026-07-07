//! acrally-agent: hybrid Assetto Corsa Rally club companion.
//!
//! Shared memory drives the live session (start on movement, stream heartbeats);
//! the game's save file supplies the authoritative penalised result on finish.
//! The telemetry source is the only platform-specific piece — on Windows it reads
//! shared memory, elsewhere (or with `mock = true`) it replays a scripted session.

// The distributed build is a GUI app (tray + window), so mark it as a Windows
// GUI-subsystem binary: launching it must NOT pop a console window. Gated to the
// `ui` feature so the headless/diagnostic build keeps its console. On non-Windows
// targets the attribute is inert.
#![cfg_attr(feature = "ui", windows_subsystem = "windows")]

mod config;
mod logfile;
mod model;
mod pairing;
mod runner;
mod savegame;
mod selfupdate;
mod session;
mod status;
mod submit;
mod telemetry;

#[cfg(feature = "ui")]
mod races;

#[cfg(feature = "ui")]
mod ui;

#[cfg(all(windows, feature = "shm"))]
mod shm;

use std::time::{Duration, Instant};

use anyhow::Result;

use config::Config;
use model::{is_real_laptime, Frame, SimStatus};
use runner::Runner;
use telemetry::{MockSource, TelemetrySource};

fn main() -> Result<()> {
    // With `windows_subsystem = "windows"` the GUI build has no console, so a
    // double-click shows only the tray UI. But the same exe still has console
    // paths — `pair`, `update`, `ACRALLY_CHECKSAVE`/`SCAN`/`DUMP`, `headless` — so
    // if it was launched from an existing terminal, reattach to that terminal's
    // console. It's a no-op when there's no parent console (the double-click case),
    // which keeps that path windowless.
    #[cfg(all(windows, feature = "ui"))]
    attach_parent_console();

    let cfg = Config::load()?;

    // `acrally-agent update`: check for a newer signed build, apply it, and relaunch.
    if std::env::args().nth(1).as_deref() == Some("update") {
        return selfupdate::run_update();
    }

    // Device pairing: `acrally-agent pair` (or ACRALLY_PAIR=1) links this agent to a
    // user account and writes the returned api_key into the config file, then exits.
    let pair_requested =
        std::env::args().nth(1).as_deref() == Some("pair") || std::env::var("ACRALLY_PAIR").is_ok();
    if pair_requested {
        return pairing::run(&cfg);
    }

    // Best-effort update nudge on the distributed (Windows) build. Non-blocking;
    // silent unless a newer release is published.
    #[cfg(windows)]
    selfupdate::spawn_background_check();

    // Gentle nudge when unauthenticated against a backend that expects a key.
    if cfg.api_key.is_none() {
        eprintln!(
            "note: no api_key set — run `acrally-agent pair` to link this agent to your \
             account (not needed for an open/dev backend)."
        );
    }

    // One-shot self-check (ACRALLY_CHECKSAVE=1): print the resolved save path and
    // the newest record, then exit. No game needed — confirms the agent reads the
    // same file/record you expect.
    if std::env::var("ACRALLY_CHECKSAVE").is_ok() {
        check_save(&cfg);
        return Ok(());
    }

    // One agent per user — a second instance would double-post every result and
    // fight over the persisted floor. (The transient subcommands above — pair,
    // update, checksave — are allowed alongside a running agent.)
    if !single_instance() {
        logfile::agent_log!("another acrally-agent instance is already running — exiting");
        already_running_notice();
        return Ok(());
    }

    logfile::agent_log!(
        "acrally-agent v{} starting — backend {} (log: {})",
        env!("CARGO_PKG_VERSION"),
        cfg.api_base,
        logfile::path().display(),
    );

    // Normal running goes to the tray UI. Console diagnostics (scan/dump) and an
    // explicit `headless` setting keep the original console loop.
    #[cfg(feature = "ui")]
    {
        let diag = std::env::var("ACRALLY_SCAN").is_ok() || std::env::var("ACRALLY_DUMP").is_ok();
        if !cfg.headless && !diag {
            return ui::run(cfg);
        }
    }

    run_headless(cfg)
}

/// Claim this user's single-instance slot. Uses a named mutex, which Windows
/// frees automatically when the process dies (a lock file could go stale after a
/// crash); the handle is deliberately never closed so the claim lives exactly as
/// long as the process. `Local\` scopes it to the current desktop session.
/// Returns `false` when another instance already holds the slot; fails open if
/// the mutex can't be created at all — better two agents than none.
#[cfg(windows)]
fn single_instance() -> bool {
    const ERROR_ALREADY_EXISTS: u32 = 183;
    extern "system" {
        fn CreateMutexW(
            attrs: *mut core::ffi::c_void,
            initial_owner: i32,
            name: *const u16,
        ) -> *mut core::ffi::c_void;
        fn GetLastError() -> u32;
    }
    let name: Vec<u16> = "Local\\acrally-agent-single-instance\0"
        .encode_utf16()
        .collect();
    unsafe {
        let handle = CreateMutexW(core::ptr::null_mut(), 0, name.as_ptr());
        if handle.is_null() {
            return true;
        }
        GetLastError() != ERROR_ALREADY_EXISTS
    }
}

/// Dev builds on other platforms don't self-distribute; skip the check.
#[cfg(not(windows))]
fn single_instance() -> bool {
    true
}

/// Tell the user why this launch did nothing. The GUI build has no console, so a
/// double-click on an already-running agent would otherwise be indistinguishable
/// from a broken exe — pop a message box pointing at the tray instead.
#[cfg(all(windows, feature = "ui"))]
fn already_running_notice() {
    const MB_ICONINFORMATION: u32 = 0x40;
    #[link(name = "user32")]
    extern "system" {
        fn MessageBoxW(
            hwnd: *mut core::ffi::c_void,
            text: *const u16,
            caption: *const u16,
            flags: u32,
        ) -> i32;
    }
    let wide = |s: &str| {
        s.encode_utf16()
            .chain(std::iter::once(0))
            .collect::<Vec<u16>>()
    };
    let text = wide("acrally-agent is already running — look for its icon in the system tray.");
    let caption = wide("acrally-agent");
    unsafe {
        MessageBoxW(
            std::ptr::null_mut(),
            text.as_ptr(),
            caption.as_ptr(),
            MB_ICONINFORMATION,
        );
    }
}

#[cfg(not(all(windows, feature = "ui")))]
fn already_running_notice() {
    eprintln!("acrally-agent is already running — exiting.");
}

/// Attach to the parent process's console if it has one, so console output from a
/// GUI-subsystem exe launched from a terminal is still visible. No-op (returns 0)
/// when launched without a console, e.g. a double-click.
#[cfg(all(windows, feature = "ui"))]
fn attach_parent_console() {
    // ATTACH_PARENT_PROCESS = (DWORD)-1; AttachConsole lives in kernel32.
    const ATTACH_PARENT_PROCESS: u32 = 0xFFFF_FFFF;
    extern "system" {
        fn AttachConsole(dwProcessId: u32) -> i32;
    }
    unsafe {
        AttachConsole(ATTACH_PARENT_PROCESS);
    }
}

/// The console loop: poll telemetry, drive the runner, optionally print a
/// verbose heartbeat or a shared-memory scan. Used for a non-`ui` build,
/// `headless = true`, or the scan/dump diagnostics.
fn run_headless(cfg: Config) -> Result<()> {
    let mut source = build_source(&cfg);
    println!(
        "acrally-agent v{} | source: {} | backend: {}",
        env!("CARGO_PKG_VERSION"),
        source.name(),
        cfg.api_base,
    );

    // One-shot raw byte dump for offset diagnosis (ACRALLY_DUMP=1), run on track.
    #[cfg(all(windows, feature = "shm"))]
    if std::env::var("ACRALLY_DUMP").is_ok() {
        shm::dump_hex();
    }

    // Repeated segment scan (ACRALLY_SCAN=1) for locating shared-memory fields.
    let scan_mode = std::env::var("ACRALLY_SCAN").is_ok();
    if scan_mode {
        println!("scan mode: dumping shared-memory segments every 2s");
    }

    let mut runner = Runner::new(cfg.clone());
    if cfg.verbose {
        println!("verbose: heartbeat every second so you can confirm the game is being read");
    }

    let interval = cfg.poll_interval();
    let mut last_beat: Option<Instant> = None;
    let mut last_scan: Option<Instant> = None;
    loop {
        let polled = source.poll();
        match &polled {
            Some(frame) => runner.on_frame(frame),
            // No telemetry (menus / the result screen at stage end): feed a blank frame so an open
            // run finalises and its save result is read, rather than hanging until superseded.
            None => runner.on_frame(&Frame::blank()),
        }

        if scan_mode {
            if last_scan.map_or(true, |t| t.elapsed() >= Duration::from_secs(2)) {
                last_scan = Some(Instant::now());
                #[cfg(all(windows, feature = "shm"))]
                shm::scan();
                #[cfg(not(all(windows, feature = "shm")))]
                println!("scan mode needs a Windows build with --features shm");
            }
        } else if cfg.verbose && last_beat.map_or(true, |t| t.elapsed() >= Duration::from_secs(1)) {
            last_beat = Some(Instant::now());
            match &polled {
                Some(f) => println!("{}", heartbeat(f)),
                None => println!("[waiting] game not detected (shared memory unavailable)"),
            }
        }

        std::thread::sleep(interval);
    }
}

/// One-line liveness summary for verbose mode.
fn heartbeat(f: &Frame) -> String {
    // AC Rally's `status` enum is unreliable (reads "off" while driving), so
    // derive the displayed state from the data: a running timer or real speed.
    let status = if is_real_laptime(&f.current_laptime) || f.speed_kmh > 1.0 {
        "driving"
    } else {
        match f.status {
            SimStatus::Off => "idle",
            SimStatus::Replay => "replay",
            SimStatus::Live => "live",
            SimStatus::Pause => "pause",
            SimStatus::Unknown(_) => "unknown",
        }
    };
    let loc = if f.track.is_empty() {
        "-".to_string()
    } else {
        f.track.clone()
    };
    let dash = |s: &str| {
        if s.is_empty() {
            "--".to_string()
        } else {
            s.to_string()
        }
    };
    format!(
        "[{status}] {loc} | {} | {} | {:.0} km/h  gear {}  {} rpm | cur {}  last {}  dist {:.0}m",
        dash(&f.car),
        dash(&f.driver),
        f.speed_kmh,
        f.gear,
        f.rpm,
        dash(&f.current_laptime),
        dash(&f.last_laptime),
        f.distance_m,
    )
}

/// Print the resolved save path and the newest parsed record, then return.
fn check_save(cfg: &Config) {
    let fmt = |ms: u32| format!("{}:{:06.3}", ms / 60_000, (ms % 60_000) as f32 / 1000.0);
    match savegame::locate_save(cfg.save_path.as_deref()) {
        Some(path) => {
            println!("save file: {}", path.display());
            match std::fs::read(&path) {
                Ok(bytes) => {
                    let recs = savegame::parse_records(&bytes);
                    println!("records found: {}", recs.len());
                    match recs.iter().max_by_key(|r| r.timestamp_ticks) {
                        Some(r) => println!(
                            "newest: {} / {}  raw {} + {}s = total {}  (ticks {})",
                            if r.stage.is_empty() {
                                "(no stage)"
                            } else {
                                &r.stage
                            },
                            r.car,
                            fmt(r.raw_ms),
                            r.penalty_ms / 1000,
                            fmt(r.total_ms),
                            r.timestamp_ticks,
                        ),
                        None => println!("no records parsed — the save format may have changed"),
                    }
                }
                Err(e) => println!("could not read save: {e}"),
            }
        }
        None => println!(
            "save file NOT found via auto-detect — set save_path in config.toml \
             (expected %LOCALAPPDATA%\\acr\\Saved\\SaveGames\\PlayerDataSaveSlot.sav)"
        ),
    }
}

/// Select the telemetry source for this platform / config.
pub(crate) fn build_source(cfg: &Config) -> Box<dyn TelemetrySource> {
    if cfg.mock {
        return Box::new(MockSource::new());
    }
    #[cfg(all(windows, feature = "shm"))]
    {
        Box::new(shm::ShmSource::new())
    }
    #[cfg(not(all(windows, feature = "shm")))]
    {
        eprintln!("note: shared-memory source not compiled in (build with --features shm on Windows); using mock");
        Box::new(MockSource::new())
    }
}
