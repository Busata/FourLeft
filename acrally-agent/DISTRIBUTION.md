# Distributing & updating acrally-agent

The agent ships as a single Windows `.exe` that **self-updates** from a signed
manifest on fourleft.io. This doc covers building a release, signing it, and
publishing it. For the design rationale see the plan; this is the operational
checklist.

## Local build vs CI

You can build and publish releases yourself from a Windows machine — GitHub
Actions is not required. CI buys automation, a clean reproducible environment,
and an audit trail, but **not** a security guarantee: what protects users from a
tampered binary is the minisign signature, and that's the same wherever the build
runs. For a solo beta, building locally is actually a touch safer because the
**minisign secret key never leaves your machine**. Move to CI later if the manual
steps get tedious.

## What makes the exe self-contained

- `--features ui,shm` is the real user build (tray UI + Windows shared-memory reader).
- `.cargo/config.toml` statically links the MSVC CRT, so there's no VC++
  redistributable dependency.
- `tray-icon` uses native Win32 and `shm` maps the segments directly, so there's
  no other runtime dependency. The result is one file: `acrally-agent.exe`.

## Where it installs (and why it can self-update without admin)

Install per-user under **`%LOCALAPPDATA%\acrally\`** — e.g. drop the exe there, or
have a future installer put it there. Because that directory is user-writable, the
agent can replace its own exe with **no UAC prompt**. The config file (with the
paired API key) lives next to it at `%LOCALAPPDATA%\acrally\config.toml`, a stable
location independent of the working directory, so it survives updates and moves.

## One-time setup: signing key

Generate a minisign keypair once and keep the **secret key offline** (a password
manager / an encrypted file — never in the repo or in CI without protection):

```powershell
minisign -G -p acrally-agent.pub -s acrally-agent.key
```

Copy the base64 body of the **public** key (the second line of `acrally-agent.pub`)
into `UPDATE_PUBLIC_KEY` in `src/selfupdate.rs`. Until that constant is set, the
agent refuses to self-update (it will still *notice* and report a new version).

## Cutting a release (automated)

Bump `version` in `Cargo.toml`, then run **one** of these. Both build the
windows-msvc exe with `--features ui,shm`, sign it, write `latest.json`, and
publish exe + `.minisig` first and the manifest last.

**From WSL (the usual path)** — via the `fourleft` CLI:

```sh
fourleft          # -> "Release AC Rally agent"
```

It asks for release notes, rsyncs the crate to the Windows build dir, builds +
signs there via `release.ps1` (WSL interop), and uploads to
`veevi:/mnt/docker-data/acrally-agent/`. See [How the WSL→Windows build
works](#how-the-wslwindows-build-works). (`devops/cli/release-agent.sh`.)

**Directly on Windows** — run the build/sign script and let it upload:

```powershell
./release.ps1 -Notes "What changed" -Upload
```

Both pick up the version from `Cargo.toml` automatically. You'll be prompted once
for your minisign key password during signing. Running agents then pick the
release up on their next check; `acrally-agent update` downloads it, verifies the
signature, swaps the exe, and relaunches.

### How the WSL→Windows build works

The MSVC exe must be built on Windows, but releases are driven from WSL. The
`fourleft` CLI bridges the two on the same PC:

1. **rsync** the crate from its WSL path to a Windows-side working copy under
   `/mnt/c` (default `/mnt/c/build/acrally-agent`). Because `/mnt/c` is the
   mounted Windows disk, this is a plain local copy — no SSH, no rsync daemon on
   Windows. `target/` and `dist/` are excluded so the Windows build cache and
   prior artifacts survive between releases.
2. **Build + sign** by invoking `release.ps1` through `powershell.exe` (WSL
   interop). `wslpath -w` converts the build dir to a `C:\…` path for PowerShell.
   The native Windows Rust toolchain + minisign (with the offline secret key) run
   there; artifacts land in `…\dist\`, which WSL sees at
   `/mnt/c/build/acrally-agent/dist/`.
3. **Upload** those artifacts from WSL to `veevi` over SSH (the same host as
   `fourleft` deploys use).

Per-machine overrides (build dir, key path) go in `release.conf` (gitignored;
see `release.conf.example`). Windows prereqs: Rust with the
`x86_64-pc-windows-msvc` target, `minisign` on PATH, and the minisign secret key
(default `%USERPROFILE%\.minisign\acrally-agent.key`).

## Files served from fourleft.io

| Path | Cache | Purpose |
|---|---|---|
| `/acrally-agent/latest.json` | ~60s | Latest-release pointer the agent polls |
| `/acrally-agent/acrally-agent-<version>.exe` | immutable | The signed build |
| `/acrally-agent/acrally-agent-<version>.exe.minisig` | immutable | Detached minisign signature |

The manifest URL is overridable at runtime with `ACRALLY_UPDATE_URL` for testing
against a local/staging file.

## How the client update works (`src/selfupdate.rs`)

1. GET `latest.json`, semver-compare `version` against the compiled version.
2. If newer: download the exe into memory, download `<url>.minisig`.
3. **Verify** the minisign signature over the exact bytes against the compiled-in
   public key — abort on any mismatch. (minisign signs the file's hash, so a
   separate checksum would be redundant.)
4. Stage to a temp file, `self-replace` swaps it in for the running exe, then the
   process relaunches the new binary and exits.

## Exe icon & metadata

`build.rs` embeds `assets/car.ico` and version/product metadata into the exe on
Windows targets (via `winresource`), so it shows the car icon and a real
publisher/description in Explorer and the download dialog. The version fields come
from `Cargo.toml` automatically. Regenerate the icon with
`python3 assets/generate_icon.py` if the car art changes.

## Not yet done (later phases)

- Periodic in-session update checks (startup check + manual button exist).
- Backend enforcement of `min_supported` (the agent already sends `agent_version`).
- Authenticode code-signing to clear the SmartScreen "unknown publisher" warning.
- Optional installer (Inno Setup / MSIX) with Start-Menu entry + auto-start.
