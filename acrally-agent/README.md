# acrally-agent

Local companion agent for an Assetto Corsa Rally club system. It runs on each
member's Windows PC and reports their time-trial runs to the club backend — a
**hybrid** of live shared-memory telemetry and the game's own save file:

- **Shared memory** drives the *live session*: as soon as you start driving it
  opens a session on the server and streams heartbeats (speed, gear, rpm, current
  stage time, distance).
- **The save file** supplies the *authoritative result*: when a stage finishes,
  the agent reads the penalised total time (raw + penalty) and sector data that
  Rally writes to `PlayerDataSaveSlot.sav`, and posts it.

Why both? AC Rally's shared memory exposes the *raw* driving time but **not
penalties** (verified exhaustively). The save file has the official penalty-
inclusive time but only after the stage ends. Together you get a live session and
a correct final result.

It never touches the game's network traffic — only the sanctioned local
shared-memory interface and the local save file.

## Flow

```
                    ┌──────────────── shared memory (live) ───────────────┐
 start driving ───▶ POST /sessions ───▶ heartbeats ───▶ finish detected ──┘
                                                              │
 restart? ─▶ POST /sessions/{id}/abort                        ▼
                                          save file rewritten ─▶ parse newest
                                                                 record ─▶
                                                    POST /sessions/{id}/result
```

- **Driving detected** (stage timer running) → `POST /sessions` → `{ session_id }`.
- **While driving** → `POST /sessions/{id}/heartbeat` every second.
- **Finish** (timer freezes / clears) → wait for the save file to update → parse
  the newest record → `POST /sessions/{id}/result` (penalised total + breakdown).
- **Restart** (timer resets to ~0 mid-run — no save is written) → `POST
  /sessions/{id}/abort` and open a fresh session.

A failed result POST is retried a few times in-process, then dropped. Results
are deliberately **not** persisted to disk for later replay — an editable spool
file would be an easy way to forge or tamper with results.

## Server API

| Method & path | Body | Purpose |
|---|---|---|
| `POST /sessions` | `{ driver, car, stage, track, started_at_ms, agent_version }` | Open a session; return `{ session_id }` |
| `POST /sessions/{id}/heartbeat` | `{ current_ms?, speed_kmh, gear, rpm, distance_m }` | Live telemetry |
| `POST /sessions/{id}/result` | `{ stage, car, driver, raw_ms, penalty_ms, total_ms, timestamp_ticks, agent_version }` | Final penalised result (`timestamp_ticks` is a stable de-dupe key) |
| `POST /sessions/{id}/abort` | `{ reason }` | Session was restarted / abandoned |

If `POST /sessions` doesn't respond, the agent uses a local `local-<nanos>`
session id so heartbeats and the result POST can still be keyed.

## Linking the agent (pairing)

Backends that authenticate members (like fourleft.io) need a personal API key.
Rather than copy-pasting one, the agent uses a device-authorization flow — the
same "enter this code / click this link" pattern as logging a TV into an account.

**In the app (what users do):** launch the app; on first run, with no key, it
shows a **Connect** screen. Click *Connect* → it opens your browser to the approval
page and shows a short code. Approve it while signed in (a linked Steam account is
required) and the agent saves the key to `config.toml` and starts reporting — no
terminal involved.

**Headless / fallback (CLI):**

```powershell
acrally-agent pair            # or: cargo run -- pair
```

Prints the same code + link, polls, and writes the key to `config.toml`.

| Method & path | Body | Purpose |
|---|---|---|
| `POST /agent/pair/start` | `{ label? }` | Begin pairing → `{ deviceCode, userCode, verificationUri, verificationUriComplete, intervalSeconds, expiresInSeconds }` |
| `POST /agent/pair/token` | `{ deviceCode }` | Poll → `{ status, apiKey?, label? }` where `status` is `pending`/`approved`/`denied`/`expired`/`consumed` |

If `api_key` is unset the agent prints a one-line reminder at startup. An
open/dev backend (e.g. `dev-server.py`) needs no key, so pairing is optional there.

The config file lives at `%LOCALAPPDATA%\acrally\config.toml` (on non-Windows,
`~/.config/acrally/config.toml`), overridable with `ACRALLY_CONFIG`. Pairing
creates it; nothing in it needs hand-editing for normal use.

## Install & self-update

Distributed as a single Windows `.exe` that self-updates from a signed manifest
on fourleft.io. `acrally-agent update` checks for a newer signed build, verifies
its minisign signature, replaces the running exe, and relaunches; the Windows
build also checks in the background at startup and nudges when one is available.
Build/sign/release steps are in [`DISTRIBUTION.md`](DISTRIBUTION.md).

## Modules

| File | Role |
|---|---|
| `telemetry.rs` | `TelemetrySource` trait + `MockSource` (scripted, runs anywhere) |
| `logfile.rs` | Persistent `agent.log` (config dir) — every pipeline event, timestamped |
| `shm.rs` | Windows-only reader of the classic `acpmf_*` shared memory |
| `session.rs` | Session state machine: Start / Progress / Restart / Finish |
| `savegame.rs` | Locate + parse `PlayerDataSaveSlot.sav` (penalised records) |
| `submit.rs` | Backend client (session endpoints) |
| `selfupdate.rs` | Poll the signed release manifest; verify + swap + relaunch |
| `runner.rs` | Orchestrates shared memory + save file into the flow above |
| `status.rs` | Live status snapshot shared with the UI |
| `ui.rs` | Window GUI (behind the `ui` feature) |
| `config.rs`, `main.rs`, `model.rs` | Config, wiring, shared types |

## Run it

```powershell
cargo run                              # mock source (no game/LLVM needed)
cargo run --features shm               # Windows: read real shared memory (needs LLVM)
```

The default build uses the **mock** source everywhere — good for a first run and
for developing on Linux/WSL. The real Windows shared-memory reader is behind the
`shm` feature (it needs LLVM/libclang; install with `winget install LLVM.LLVM`).

Verbose heartbeat (confirm the game is being read while you drive):

```powershell
$env:ACRALLY_VERBOSE=1; cargo run --features shm
```

Every pipeline event (session started / restart / finish, save-record sightings,
aborts with their reason, result POSTs, telemetry stale/resumed) is also appended
to **`%LOCALAPPDATA%\acrally\agent.log`** with a UTC timestamp — the windowless
GUI build has no console, so when a run doesn't show up on the leaderboard this
file is the place to see what the agent actually observed. It's rotated to
`agent.log.old` at startup once it passes ~1 MB.

## Window UI

A status window is available behind the `ui` feature. It runs the telemetry
pipeline on a background thread and shows the live state (driving / idle, car,
speed, backend connection, last posted result). On first run without a key it
shows the **Connect** (pairing) screen described above. Closing the window quits
the agent. The window icon is a small car (`assets/car.svg`, mirrored as an RGBA
buffer in `src/ui.rs`). A **Races** tab is stubbed for the planned per-club event
browser.

```powershell
cargo run --features ui             # windowed app, mock source
cargo run --features ui,shm         # windowed app + real shared memory (Windows)
```

The UI is **off by default** so the core pipeline builds and tests run without a
display or GUI system libraries. When built with `ui`, the window is the default
way to run; the console loop still runs for the `scan`/`dump`/`checksave`
diagnostics, or set `headless = true` (or `ACRALLY_HEADLESS=1`).

- **Windows** (the target): no extra system packages.
- **Linux/WSL** (dev only): building the `ui` feature needs a display to actually
  show the window.

## Testing locally

`dev-server.py` is a zero-dependency local backend that implements the four
endpoints, logs every request, and serves a live leaderboard at
<http://127.0.0.1:8799/>. Run it in one terminal:

```sh
python3 dev-server.py
```

Point the agent at it (`ACRALLY_API_BASE=http://127.0.0.1:8799`, or set
`api_base` in `config.toml`), then in another terminal run the agent. Two ways
to drive it:

**A. Full flow, real game (Windows):** `cargo run --features shm`, then drive a
stage. You'll see `START` → `HB` lines → `RESULT` in the server console, and the
run appears on the leaderboard page.

**B. Without the game (any OS):** set `mock = true` and point `save_path` at a
copy of a real save (e.g. `tests/fixtures/PlayerDataSaveSlot.sav`). The mock
drives a scripted session; when it "finishes", `touch` the save file to simulate
the game writing it, and the agent posts the result from that save. This is
exactly how the flow is verified in development.

Open <http://127.0.0.1:8799/> in a browser to watch sessions and the leaderboard
update live (auto-refreshes every 2s).

## The save file

Auto-detected at `%LOCALAPPDATA%\acr\Saved\SaveGames\PlayerDataSaveSlot.sav`
(override with `save_path`). It's an Unreal GVAS binary save; the parser anchors
on each record's timestamp and reads fixed-offset fields — a layout validated
against real saves (raw time, penalty, total, stage id, car). The newest record
(largest timestamp) is the run that just completed.

## On-device notes

The shared-memory field offsets and the save-file record layout were reverse-
engineered against a specific early-access build. If a game update shifts them,
the two places to check are `src/shm.rs` (live fields) and `src/savegame.rs`
(result records). Everything else keys off the normalized `Frame` / `StageRecord`.
