//! Tray + window UI (built only with the `ui` feature).
//!
//! Layout of responsibility:
//!   - a background thread runs the telemetry pipeline and publishes a live
//!     [`AgentStatus`] snapshot;
//!   - the main thread owns the event loop: an `eframe` window that renders that
//!     snapshot, plus a `tray-icon` entry so the app lives in the system tray.
//!     Closing the window hides it to the tray rather than quitting.
//!
//! Tray events (menu clicks + a left-click on the icon) are forwarded to the egui
//! context via `request_repaint`, so they wake the window even while it's hidden —
//! otherwise a hidden window never ticks and "Open" would do nothing.
//!
//! First run without an `api_key` shows an in-window **Connect** screen that drives
//! the device-pairing flow (no CLI): it shows a code + link, opens the browser, and
//! once approved saves the key and starts reporting.

use std::sync::atomic::{AtomicBool, Ordering};
use std::sync::{Arc, Mutex};
use std::thread::JoinHandle;
use std::time::Duration;

use anyhow::{anyhow, Result};
use eframe::egui;
use tray_icon::menu::{Menu, MenuEvent, MenuId, MenuItem, PredefinedMenuItem};
use tray_icon::{Icon, MouseButton, MouseButtonState, TrayIcon, TrayIconBuilder, TrayIconEvent};

use crate::config::Config;
use crate::pairing::{self, Phase};
use crate::runner::Runner;
use crate::selfupdate::{self, UpdateState};
use crate::status::{AgentStatus, DriveState, StatusHandle};

/// Display name shown in the window title bar and tray tooltip.
const APP_NAME: &str = "Fourleft.IO - AC Rally Companion";

/// An action the tray raised, to be applied on the UI thread.
enum TrayAction {
    Open,
    Quit,
}

/// Launch the tray app: build the tray, then run the UI event loop. The telemetry
/// pipeline starts once we have a key (immediately if configured, else after pairing).
pub fn run(cfg: Config) -> Result<()> {
    let status: StatusHandle = Arc::new(Mutex::new(AgentStatus::default()));
    let stop = Arc::new(AtomicBool::new(false));
    let pairing = Arc::new(Mutex::new(Phase::Idle));
    let actions: Arc<Mutex<Vec<TrayAction>>> = Arc::new(Mutex::new(Vec::new()));

    // Build the tray icon + menu on the main thread.
    let open_item = MenuItem::new("Open", true, None);
    let quit_item = MenuItem::new("Quit", true, None);
    let open_id = open_item.id().clone();
    let quit_id = quit_item.id().clone();
    let menu = Menu::new();
    menu.append(&open_item)
        .and_then(|_| menu.append(&PredefinedMenuItem::separator()))
        .and_then(|_| menu.append(&quit_item))
        .map_err(|e| anyhow!("tray menu: {e}"))?;
    let tray = TrayIconBuilder::new()
        .with_menu(Box::new(menu))
        .with_tooltip(APP_NAME)
        .with_icon(car_icon()?)
        .build()
        .map_err(|e| anyhow!("tray icon: {e}"))?;

    let native_options = eframe::NativeOptions {
        viewport: egui::ViewportBuilder::default()
            .with_title(APP_NAME)
            .with_inner_size([440.0, 560.0])
            .with_min_inner_size([360.0, 420.0])
            .with_icon(Arc::new(car_icon_data(64))),
        ..Default::default()
    };

    // Check for a newer signed build in the background so the window can offer an
    // in-app "Update & restart". Only on Windows (the distributed target); the
    // manual "Check for updates" button works everywhere for dev testing.
    let update = Arc::new(Mutex::new(UpdateState::default()));
    #[cfg(windows)]
    {
        let u = update.clone();
        std::thread::spawn(move || selfupdate::drive_check(u));
    }

    let linked = cfg.api_key.is_some();
    let app = App {
        cfg,
        status,
        stop: stop.clone(),
        _tray: tray,
        tab: Tab::Status,
        pairing: pairing.clone(),
        actions: actions.clone(),
        linked,
        pipeline: None,
        update,
    };

    let result = eframe::run_native(
        "acrally-agent",
        native_options,
        Box::new(move |cc| {
            // Wake the (possibly hidden) window on any tray/menu event.
            install_event_forwarders(&cc.egui_ctx, actions, open_id, quit_id);
            Ok(Box::new(app))
        }),
    )
    .map_err(|e| anyhow!("eframe failed: {e}"));

    stop.store(true, Ordering::Relaxed);
    result
}

/// Route tray-icon + menu events into `actions` and repaint so `update` runs even
/// while the window is hidden (a hidden eframe window otherwise stops ticking).
fn install_event_forwarders(
    ctx: &egui::Context,
    actions: Arc<Mutex<Vec<TrayAction>>>,
    open_id: MenuId,
    quit_id: MenuId,
) {
    {
        let ctx = ctx.clone();
        let actions = actions.clone();
        MenuEvent::set_event_handler(Some(move |event: MenuEvent| {
            let action = if event.id == open_id {
                Some(TrayAction::Open)
            } else if event.id == quit_id {
                Some(TrayAction::Quit)
            } else {
                None
            };
            if let Some(action) = action {
                if let Ok(mut queue) = actions.lock() {
                    queue.push(action);
                }
                ctx.request_repaint();
            }
        }));
    }
    {
        let ctx = ctx.clone();
        TrayIconEvent::set_event_handler(Some(move |event: TrayIconEvent| {
            // A left-click (button released) on the icon opens the window.
            if let TrayIconEvent::Click {
                button: MouseButton::Left,
                button_state: MouseButtonState::Up,
                ..
            } = event
            {
                if let Ok(mut queue) = actions.lock() {
                    queue.push(TrayAction::Open);
                }
                ctx.request_repaint();
            }
        }));
    }
}

/// Telemetry pipeline: poll the source, drive the runner, publish live status.
fn pipeline_loop(cfg: Config, status: StatusHandle, stop: Arc<AtomicBool>) {
    let mut source = crate::build_source(&cfg);
    if let Ok(mut s) = status.lock() {
        s.source = source.name().to_string();
        s.backend = cfg.api_base.clone();
        s.agent_version = env!("CARGO_PKG_VERSION").to_string();
        s.save_path =
            crate::savegame::locate_save(cfg.save_path.as_deref()).map(|p| p.display().to_string());
    }

    let mut runner = Runner::with_status(cfg.clone(), Some(status.clone()));
    let interval = cfg.poll_interval();
    while !stop.load(Ordering::Relaxed) {
        match source.poll() {
            Some(f) => {
                runner.on_frame(&f);
                if let Ok(mut s) = status.lock() {
                    s.apply_frame(&f);
                }
            }
            None => {
                if let Ok(mut s) = status.lock() {
                    s.mark_no_game();
                }
            }
        }
        std::thread::sleep(interval);
    }
}

#[derive(PartialEq, Eq)]
enum Tab {
    Status,
    Races,
}

/// What the Connect screen asked us to do (applied outside the render closure).
enum ConnectAction {
    None,
    Connect,
    Skip,
}

struct App {
    cfg: Config,
    status: StatusHandle,
    stop: Arc<AtomicBool>,
    _tray: TrayIcon,
    tab: Tab,
    pairing: Arc<Mutex<Phase>>,
    actions: Arc<Mutex<Vec<TrayAction>>>,
    /// True once we have a key (or the user chose to run without one): show the app.
    linked: bool,
    pipeline: Option<JoinHandle<()>>,
    /// Self-update state, driven by background threads (see `selfupdate`).
    update: Arc<Mutex<UpdateState>>,
}

impl App {
    fn show_window(ctx: &egui::Context) {
        ctx.send_viewport_cmd(egui::ViewportCommand::Minimized(false));
        ctx.send_viewport_cmd(egui::ViewportCommand::Visible(true));
        ctx.send_viewport_cmd(egui::ViewportCommand::Focus);
    }

    fn quit(&self) -> ! {
        self.stop.store(true, Ordering::Relaxed);
        std::process::exit(0);
    }

    fn spawn_pipeline(&mut self) {
        if self.pipeline.is_some() {
            return;
        }
        let cfg = self.cfg.clone();
        let status = self.status.clone();
        let stop = self.stop.clone();
        self.pipeline = Some(std::thread::spawn(move || pipeline_loop(cfg, status, stop)));
    }

    fn start_pairing(&self) {
        if let Ok(mut p) = self.pairing.lock() {
            *p = Phase::Connecting;
        }
        let api_base = self.cfg.api_base.clone();
        let shared = self.pairing.clone();
        std::thread::spawn(move || pairing::drive(api_base, shared));
    }

    /// Kick off a background "is there a newer build?" check.
    fn start_update_check(&self) {
        let u = self.update.clone();
        std::thread::spawn(move || selfupdate::drive_check(u));
    }

    /// Download + verify + apply the latest update; the app relaunches on success.
    fn start_update_apply(&self) {
        let u = self.update.clone();
        std::thread::spawn(move || selfupdate::drive_apply(u));
    }

    fn render_app(&mut self, ctx: &egui::Context) {
        let snap = self.status.lock().map(|s| s.clone()).unwrap_or_default();
        let update = self.update.lock().map(|s| s.clone()).unwrap_or_default();
        let mut do_check = false;
        let mut do_apply = false;

        // Update banner: only shown when there's something to act on or report.
        match &update {
            UpdateState::Available { version, notes } => {
                egui::TopBottomPanel::top("update-banner").show(ctx, |ui| {
                    ui.add_space(5.0);
                    ui.horizontal_wrapped(|ui| {
                        ui.label(
                            egui::RichText::new(format!("Update available — v{version}")).strong(),
                        );
                        if ui.button("Update & restart").clicked() {
                            do_apply = true;
                        }
                    });
                    if !notes.is_empty() {
                        ui.label(egui::RichText::new(notes).weak());
                    }
                    ui.add_space(5.0);
                });
            }
            UpdateState::Downloading { version } => {
                egui::TopBottomPanel::top("update-banner").show(ctx, |ui| {
                    ui.add_space(5.0);
                    ui.horizontal(|ui| {
                        ui.spinner();
                        ui.label(format!("Updating to v{version}… the app will restart."));
                    });
                    ui.add_space(5.0);
                });
            }
            UpdateState::Failed(msg) => {
                egui::TopBottomPanel::top("update-banner").show(ctx, |ui| {
                    ui.add_space(5.0);
                    ui.colored_label(
                        egui::Color32::from_rgb(0xd0, 0x6b, 0x5b),
                        format!("Update failed: {msg}"),
                    );
                    ui.add_space(5.0);
                });
            }
            UpdateState::Idle | UpdateState::Checking | UpdateState::UpToDate => {}
        }

        egui::TopBottomPanel::top("tabs").show(ctx, |ui| {
            ui.add_space(4.0);
            ui.horizontal(|ui| {
                ui.selectable_value(&mut self.tab, Tab::Status, "Status");
                ui.selectable_value(&mut self.tab, Tab::Races, "Races");
            });
            ui.add_space(2.0);
        });

        // Bottom bar: manual update check + status.
        egui::TopBottomPanel::bottom("update-bar").show(ctx, |ui| {
            ui.add_space(3.0);
            ui.horizontal(|ui| {
                if matches!(update, UpdateState::Checking) {
                    ui.spinner();
                    ui.label(egui::RichText::new("Checking for updates…").weak());
                } else {
                    if ui.button("Check for updates").clicked() {
                        do_check = true;
                    }
                    if matches!(update, UpdateState::UpToDate) {
                        ui.label(egui::RichText::new("up to date").weak());
                    }
                }
            });
            ui.add_space(3.0);
        });

        egui::CentralPanel::default().show(ctx, |ui| match self.tab {
            Tab::Status => status_tab(ui, &snap),
            Tab::Races => races_tab(ui, &self.cfg),
        });

        if do_check {
            self.start_update_check();
        }
        if do_apply {
            self.start_update_apply();
        }
    }

    fn render_connect(&mut self, ctx: &egui::Context) {
        let phase = self.pairing.lock().map(|p| p.clone()).unwrap_or_default();
        let mut action = ConnectAction::None;
        egui::CentralPanel::default().show(ctx, |ui| {
            action = connect_screen(ui, &phase);
        });
        match action {
            ConnectAction::Connect => self.start_pairing(),
            ConnectAction::Skip => self.linked = true, // run without linking (open/dev backend)
            ConnectAction::None => {}
        }
    }
}

impl eframe::App for App {
    fn update(&mut self, ctx: &egui::Context, _frame: &mut eframe::Frame) {
        // Keep ticking (~4 Hz) while visible so telemetry stays fresh. When hidden,
        // tray events wake us via request_repaint (see install_event_forwarders).
        ctx.request_repaint_after(Duration::from_millis(250));

        // Apply any queued tray actions.
        let queued: Vec<TrayAction> = self
            .actions
            .lock()
            .map(|mut q| q.drain(..).collect())
            .unwrap_or_default();
        for action in queued {
            match action {
                TrayAction::Open => Self::show_window(ctx),
                TrayAction::Quit => self.quit(),
            }
        }

        // Closing the window hides it to the tray instead of exiting.
        if ctx.input(|i| i.viewport().close_requested()) {
            ctx.send_viewport_cmd(egui::ViewportCommand::CancelClose);
            ctx.send_viewport_cmd(egui::ViewportCommand::Visible(false));
        }

        // Pairing just completed -> adopt the key and start reporting.
        if !self.linked {
            let key = match self.pairing.lock().map(|p| p.clone()).unwrap_or_default() {
                Phase::Approved { api_key } => Some(api_key),
                _ => None,
            };
            if let Some(api_key) = key {
                self.cfg.api_key = Some(api_key);
                self.linked = true;
            }
        }

        if self.linked && self.pipeline.is_none() {
            self.spawn_pipeline();
        }

        if self.linked {
            self.render_app(ctx);
        } else {
            self.render_connect(ctx);
        }
    }
}

/// The first-run pairing screen. Returns an action for the caller to apply.
fn connect_screen(ui: &mut egui::Ui, phase: &Phase) -> ConnectAction {
    let mut action = ConnectAction::None;
    ui.add_space(16.0);
    ui.vertical_centered(|ui| {
        ui.heading("Connect this agent");
        ui.add_space(6.0);
        ui.label(
            egui::RichText::new("Link the agent to your fourleft.io account to report your runs.")
                .weak(),
        );
        ui.add_space(18.0);

        match phase {
            Phase::Idle => {
                if ui.button("Connect").clicked() {
                    action = ConnectAction::Connect;
                }
                ui.add_space(10.0);
                if ui.small_button("Use without an account").clicked() {
                    action = ConnectAction::Skip;
                }
            }
            Phase::Connecting => {
                ui.spinner();
                ui.label("Contacting the club…");
            }
            Phase::Waiting { user_code, url } => {
                ui.label("In your browser, sign in and confirm this code:");
                ui.add_space(8.0);
                ui.label(egui::RichText::new(user_code).heading().monospace().strong());
                ui.add_space(12.0);
                if ui.button("Open approval page").clicked() {
                    let _ = pairing::open_browser(url);
                }
                ui.add_space(6.0);
                ui.hyperlink(url);
                ui.add_space(12.0);
                ui.horizontal(|ui| {
                    ui.spinner();
                    ui.label("Waiting for approval…");
                });
            }
            Phase::Approved { .. } => {
                ui.label("Connected!");
            }
            Phase::Failed(msg) => {
                ui.colored_label(egui::Color32::from_rgb(0xd0, 0x6b, 0x5b), msg);
                ui.add_space(12.0);
                if ui.button("Try again").clicked() {
                    action = ConnectAction::Connect;
                }
            }
        }
    });
    action
}

fn status_tab(ui: &mut egui::Ui, s: &AgentStatus) {
    let (dot, _) = state_style(s.state);
    ui.add_space(6.0);
    ui.horizontal(|ui| {
        ui.label(egui::RichText::new("●").color(dot).size(18.0));
        ui.heading(s.state.label());
    });
    if !s.track.is_empty() {
        ui.label(egui::RichText::new(&s.track).strong());
    }
    ui.add_space(8.0);

    egui::Grid::new("telemetry")
        .num_columns(2)
        .spacing([18.0, 6.0])
        .show(ui, |ui| {
            row(ui, "Car", dash(&s.car));
            row(ui, "Driver", dash(&s.driver));
            row(ui, "Speed", format!("{:.0} km/h", s.speed_kmh));
            row(ui, "Gear", s.gear.to_string());
            row(ui, "RPM", s.rpm.to_string());
            row(ui, "Current", dash(&s.current_laptime));
            row(ui, "Distance", format!("{:.0} m", s.distance_m));
        });

    ui.add_space(12.0);
    ui.separator();
    ui.add_space(8.0);

    ui.horizontal(|ui| {
        let (color, text) = if s.backend_connected {
            (egui::Color32::from_rgb(0x4c, 0xc2, 0x6a), "connected")
        } else {
            (egui::Color32::from_rgb(0xd0, 0x6b, 0x5b), "not connected")
        };
        ui.label("Backend:");
        ui.colored_label(color, text);
    });
    ui.label(egui::RichText::new(&s.backend).weak());
    ui.label(format!(
        "sessions: {}   results posted: {}",
        s.sessions_started, s.results_posted
    ));
    if let Some(r) = &s.last_result {
        ui.add_space(4.0);
        ui.label("Last result:");
        ui.label(egui::RichText::new(r).strong());
    }

    ui.add_space(12.0);
    ui.separator();
    ui.add_space(6.0);
    ui.label(
        egui::RichText::new(format!(
            "source: {}   ·   v{}",
            dash(&s.source),
            s.agent_version
        ))
        .weak(),
    );
    match &s.save_path {
        Some(p) => ui.label(egui::RichText::new(format!("save: {p}")).weak()),
        None => ui.label(
            egui::RichText::new("save file not found — results can't be read")
                .color(egui::Color32::from_rgb(0xd0, 0x9b, 0x4b)),
        ),
    };
}

fn races_tab(ui: &mut egui::Ui, cfg: &Config) {
    ui.add_space(6.0);
    ui.heading("Races");
    ui.add_space(6.0);
    ui.label("Per-club events you can enter will appear here.");
    ui.add_space(8.0);
    ui.label(
        egui::RichText::new("Not wired up yet — this needs a backend endpoint the agent can query.")
            .weak(),
    );
    ui.add_space(10.0);
    egui::Grid::new("club")
        .num_columns(2)
        .spacing([18.0, 6.0])
        .show(ui, |ui| {
            row(ui, "Backend", cfg.api_base.clone());
        });
}

fn row(ui: &mut egui::Ui, k: &str, v: String) {
    ui.label(egui::RichText::new(k).weak());
    ui.label(v);
    ui.end_row();
}

fn dash(s: &str) -> String {
    if s.is_empty() {
        "—".to_string()
    } else {
        s.to_string()
    }
}

/// Dot colour for a drive state.
fn state_style(state: DriveState) -> (egui::Color32, &'static str) {
    let c = match state {
        DriveState::Driving => egui::Color32::from_rgb(0x4c, 0xc2, 0x6a),
        DriveState::Idle => egui::Color32::from_rgb(0x8a, 0x94, 0xa2),
        DriveState::Replay | DriveState::Paused => egui::Color32::from_rgb(0xd0, 0x9b, 0x4b),
        DriveState::NoGame => egui::Color32::from_rgb(0xd0, 0x6b, 0x5b),
        DriveState::Unknown => egui::Color32::from_rgb(0x8a, 0x94, 0xa2),
    };
    (c, state.label())
}

/// Tray icon: a small side-profile car rendered to RGBA (see also assets/car.svg).
fn car_icon() -> Result<Icon> {
    let n = 32;
    Icon::from_rgba(car_pixels(n), n, n).map_err(|e| anyhow!("icon: {e}"))
}

/// Window icon data (same car).
fn car_icon_data(n: u32) -> egui::IconData {
    egui::IconData {
        rgba: car_pixels(n),
        width: n,
        height: n,
    }
}

/// Draw a side-profile car into an `n`×`n` RGBA buffer. Coordinates are authored on
/// a 64-grid and scaled, so the same shape works at any icon size. Mirrors assets/car.svg.
fn car_pixels(n: u32) -> Vec<u8> {
    const GRID: f32 = 64.0;
    let green = [0x4c, 0xc2, 0x6a, 0xff];
    let glass = [0xdf, 0xf3, 0xe6, 0xff];
    let tyre = [0x22, 0x26, 0x2b, 0xff];
    let hub = [0xb6, 0xbf, 0xcb, 0xff];
    let clear = [0, 0, 0, 0];

    let wheels = [(19.0_f32, 47.0_f32), (45.0_f32, 47.0_f32)];

    let in_body = |x: f32, y: f32| -> bool {
        // Lower body.
        let lower = (7.0..=57.0).contains(&x) && (34.0..=47.0).contains(&y);
        // Cabin: trapezoid, narrower at the top.
        let inset = (34.0 - y) * 0.45;
        let cabin = (20.0..=34.0).contains(&y) && x >= 22.0 + inset && x <= 43.0 - inset;
        lower || cabin
    };
    let in_glass = |x: f32, y: f32| -> bool {
        let inset = (32.0 - y) * 0.45;
        (23.0..=32.0).contains(&y) && x >= 25.0 + inset && x <= 40.0 - inset
    };

    let mut rgba = Vec::with_capacity((n * n * 4) as usize);
    for py in 0..n {
        for px in 0..n {
            let x = (px as f32 + 0.5) * GRID / n as f32;
            let y = (py as f32 + 0.5) * GRID / n as f32;

            let mut color = clear;
            if in_body(x, y) {
                color = green;
            }
            if in_glass(x, y) {
                color = glass;
            }
            for (cx, cy) in wheels {
                let d = ((x - cx).powi(2) + (y - cy).powi(2)).sqrt();
                if d <= 8.5 {
                    color = tyre;
                }
                if d <= 3.0 {
                    color = hub;
                }
            }
            rgba.extend_from_slice(&color);
        }
    }
    rgba
}
