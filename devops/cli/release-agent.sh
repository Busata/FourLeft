#!/bin/bash

source "$FOURLEFT_DEVOPS_ROOT"/cli/common.sh

# Release the AC Rally companion agent from WSL:
#   1. rsync the crate to a Windows-side build dir (on /mnt/c),
#   2. build + sign it natively via release.ps1 (WSL interop / powershell.exe),
#   3. upload the signed exe + signature, then latest.json LAST, to the release
#      dir the reverse proxy serves at /acrally-agent/.
#
# The Windows build machine is this same PC (WSL interop). Prereqs there: Rust
# (x86_64-pc-windows-msvc), minisign, and the minisign secret key.

# Remote docker host is shared with deploy (.deploy.conf); the agent release dir is
# the host bind-mount the reverse proxy serves from.
CONF="$FOURLEFT_DEVOPS_ROOT/.deploy.conf"
[ -f "$CONF" ] && source "$CONF"
REMOTE_HOST="${REMOTE_HOST:-veevi}"
REMOTE_AGENT_DIR="${REMOTE_AGENT_DIR:-/mnt/docker-data/acrally-agent}"

# The crate lives alongside devops in the repo. release.conf (gitignored) can
# override WIN_BUILD_DIR / MINISIGN_KEY_WIN per machine.
AGENT_DIR="$(cd "$FOURLEFT_DEVOPS_ROOT/../acrally-agent" && pwd)"
[ -f "$AGENT_DIR/release.conf" ] && source "$AGENT_DIR/release.conf"
WIN_BUILD_DIR="${WIN_BUILD_DIR:-/mnt/c/build/acrally-agent}"
MINISIGN_KEY_WIN="${MINISIGN_KEY_WIN:-}"   # optional Windows-path override

function runAgentRelease() {
    local version
    version=$(grep -m1 -E '^version = "' "$AGENT_DIR/Cargo.toml" | sed -E 's/.*"([^"]+)".*/\1/')
    if [ -z "$version" ]; then
        echo "Could not read version from Cargo.toml."
        return
    fi

    local notes
    notes=$(whiptail --title "Release AC Rally agent" --inputbox \
        "Release notes for v$version (shown to users in the update prompt):" \
        10 72 "" 3>&2 2>&1 1>&3) || { echo "Cancelled."; return; }

    if ! whiptail --title "Release AC Rally agent" --yesno \
        "Build v$version on Windows, sign it, and publish to:\n\n  $REMOTE_HOST:$REMOTE_AGENT_DIR\n\nWindows build dir: $WIN_BUILD_DIR\n\nProceed?" 16 74; then
        echo "Cancelled."
        return
    fi

    # 1) Sync source to the Windows build dir. Keep Windows' own target/ and dist/
    #    so the build cache survives between releases.
    echo "==> Syncing source to $WIN_BUILD_DIR"
    mkdir -p "$WIN_BUILD_DIR" || { echo "could not create $WIN_BUILD_DIR"; return; }
    if ! rsync -a --delete \
        --exclude 'target/' --exclude 'dist/' --exclude '.git/' \
        --exclude 'config.toml' --exclude 'release.conf' \
        "$AGENT_DIR"/ "$WIN_BUILD_DIR"/; then
        echo "rsync to the Windows build dir failed."
        return
    fi

    # 2) Build + sign natively on Windows. You'll be prompted for the key password.
    echo "==> Building + signing on Windows (minisign will ask for your key password)"
    local pwsh win_path ps_args
    # WSL interop usually puts powershell.exe on PATH; fall back to its known path.
    pwsh="$(command -v powershell.exe || echo /mnt/c/Windows/System32/WindowsPowerShell/v1.0/powershell.exe)"
    if [ ! -x "$pwsh" ]; then
        echo "powershell.exe not found — is WSL interop enabled on this machine?"
        return
    fi
    win_path=$(wslpath -w "$WIN_BUILD_DIR")
    ps_args=(-NoProfile -ExecutionPolicy Bypass -File "${win_path}\\release.ps1" -Notes "$notes")
    [ -n "$MINISIGN_KEY_WIN" ] && ps_args+=(-SecretKey "$MINISIGN_KEY_WIN")
    if ! "$pwsh" "${ps_args[@]}"; then
        echo "Windows build/sign failed."
        return
    fi

    # 3) Publish: exe + signature first, manifest LAST so the pointer never leads.
    local dist exe sig manifest
    dist="$WIN_BUILD_DIR/dist"
    exe="$dist/acrally-agent-$version.exe"
    sig="$exe.minisig"
    manifest="$dist/latest.json"
    local f
    for f in "$exe" "$sig" "$manifest"; do
        if [ ! -f "$f" ]; then
            echo "Missing build artifact: $f"
            return
        fi
    done

    echo "==> Uploading to $REMOTE_HOST:$REMOTE_AGENT_DIR"
    ssh "$REMOTE_HOST" "mkdir -p $REMOTE_AGENT_DIR" || { echo "could not reach $REMOTE_HOST"; return; }
    scp "$exe" "$sig" "$REMOTE_HOST:$REMOTE_AGENT_DIR/" || { echo "upload of exe/signature failed"; return; }
    scp "$manifest" "$REMOTE_HOST:$REMOTE_AGENT_DIR/" || { echo "upload of latest.json failed"; return; }

    echo
    echo "Published acrally-agent v$version."
    echo "  https://fourleft.io/acrally-agent/latest.json"
    echo "  Agents on older versions will pick it up on their next check."
}

runAgentRelease
