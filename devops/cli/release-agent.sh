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

# Bump an X.Y.Z version by the given level. Echoes the new version; non-zero if
# the current version isn't plain semver (so the caller can bail rather than guess).
function bumpVersion() {
    local ver="$1" level="$2" major minor patch
    IFS='.' read -r major minor patch <<< "$ver"
    patch="${patch%%[-+]*}"   # drop any -pre / +build suffix
    if ! [[ "$major" =~ ^[0-9]+$ && "$minor" =~ ^[0-9]+$ && "$patch" =~ ^[0-9]+$ ]]; then
        return 1
    fi
    case "$level" in
        major) major=$((major + 1)); minor=0; patch=0 ;;
        minor) minor=$((minor + 1)); patch=0 ;;
        patch) patch=$((patch + 1)) ;;
        *) return 1 ;;
    esac
    echo "${major}.${minor}.${patch}"
}

# Restore Cargo.toml/Cargo.lock from the pre-bump backups, if any. Keeps a failed
# release from leaving a bumped-but-unreleased version behind in the working tree.
function restoreAgentVersion() {
    [ -f "$AGENT_DIR/Cargo.toml.relbak" ] && mv -f "$AGENT_DIR/Cargo.toml.relbak" "$AGENT_DIR/Cargo.toml"
    [ -f "$AGENT_DIR/Cargo.lock.relbak" ] && mv -f "$AGENT_DIR/Cargo.lock.relbak" "$AGENT_DIR/Cargo.lock"
}

function runAgentRelease() {
    local version
    version=$(grep -m1 -E '^version = "' "$AGENT_DIR/Cargo.toml" | sed -E 's/.*"([^"]+)".*/\1/')
    if [ -z "$version" ]; then
        echo "Could not read version from Cargo.toml."
        return
    fi

    # Ask what kind of release this is, and show the resulting version for each.
    local vPatch vMinor vMajor
    vPatch=$(bumpVersion "$version" patch) \
        && vMinor=$(bumpVersion "$version" minor) \
        && vMajor=$(bumpVersion "$version" major) \
        || { echo "Current version '$version' isn't plain X.Y.Z — bump Cargo.toml by hand."; return; }

    local level
    level=$(whiptail --title "Release AC Rally agent" --menu \
        "Current version: $version\n\nWhat kind of release is this?" 16 72 3 \
        "patch" "Bug fixes / no behaviour change  -> $vPatch" \
        "minor" "New features, backwards-compatible -> $vMinor" \
        "major" "Breaking changes                  -> $vMajor" \
        3>&2 2>&1 1>&3) || { echo "Cancelled."; return; }

    local oldVersion="$version" newVersion
    newVersion=$(bumpVersion "$version" "$level") || { echo "Could not compute new version."; return; }

    local notes
    notes=$(whiptail --title "Release AC Rally agent" --inputbox \
        "Release notes for v$newVersion (shown to users in the update prompt):" \
        10 72 "" 3>&2 2>&1 1>&3) || { echo "Cancelled."; return; }

    if ! whiptail --title "Release AC Rally agent" --yesno \
        "Bump v$oldVersion -> v$newVersion ($level), build + sign on Windows, and publish to:\n\n  $REMOTE_HOST:$REMOTE_AGENT_DIR\n\nWindows build dir: $WIN_BUILD_DIR\n\nProceed?" 17 74; then
        echo "Cancelled."
        return
    fi

    # Write the new version into Cargo.toml (+ the Cargo.lock package entry) before
    # the rsync, so the Windows build compiles it. Back both up first; any failure
    # below restores them so a botched release doesn't strand a bumped version.
    echo "==> Bumping version $oldVersion -> $newVersion"
    cp "$AGENT_DIR/Cargo.toml" "$AGENT_DIR/Cargo.toml.relbak" || { echo "could not back up Cargo.toml"; return; }
    sed -i '0,/^version = "/{s/^version = ".*"/version = "'"$newVersion"'"/}' "$AGENT_DIR/Cargo.toml"
    if [ -f "$AGENT_DIR/Cargo.lock" ]; then
        cp "$AGENT_DIR/Cargo.lock" "$AGENT_DIR/Cargo.lock.relbak"
        sed -i '/^name = "acrally-agent"$/{n;s/^version = ".*"/version = "'"$newVersion"'"/}' "$AGENT_DIR/Cargo.lock"
    fi
    local written
    written=$(grep -m1 -E '^version = "' "$AGENT_DIR/Cargo.toml" | sed -E 's/.*"([^"]+)".*/\1/')
    if [ "$written" != "$newVersion" ]; then
        echo "Version bump failed (Cargo.toml still shows '$written')."
        restoreAgentVersion
        return
    fi
    version="$newVersion"

    # 1) Sync source to the Windows build dir. Keep Windows' own target/ and dist/
    #    so the build cache survives between releases.
    echo "==> Syncing source to $WIN_BUILD_DIR"
    mkdir -p "$WIN_BUILD_DIR" || { echo "could not create $WIN_BUILD_DIR"; restoreAgentVersion; return; }
    if ! rsync -a --delete \
        --exclude 'target/' --exclude 'dist/' --exclude '.git/' \
        --exclude 'config.toml' --exclude 'release.conf' --exclude '*.relbak' \
        "$AGENT_DIR"/ "$WIN_BUILD_DIR"/; then
        echo "rsync to the Windows build dir failed."
        restoreAgentVersion
        return
    fi

    # 2) Build + sign natively on Windows. You'll be prompted for the key password.
    echo "==> Building + signing on Windows (minisign will ask for your key password)"
    local pwsh win_path ps_args
    # WSL interop usually puts powershell.exe on PATH; fall back to its known path.
    pwsh="$(command -v powershell.exe || echo /mnt/c/Windows/System32/WindowsPowerShell/v1.0/powershell.exe)"
    if [ ! -x "$pwsh" ]; then
        echo "powershell.exe not found — is WSL interop enabled on this machine?"
        restoreAgentVersion
        return
    fi
    win_path=$(wslpath -w "$WIN_BUILD_DIR")
    ps_args=(-NoProfile -ExecutionPolicy Bypass -File "${win_path}\\release.ps1" -Notes "$notes")
    [ -n "$MINISIGN_KEY_WIN" ] && ps_args+=(-SecretKey "$MINISIGN_KEY_WIN")
    if ! "$pwsh" "${ps_args[@]}"; then
        echo "Windows build/sign failed."
        restoreAgentVersion
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
            restoreAgentVersion
            return
        fi
    done

    echo "==> Uploading to $REMOTE_HOST:$REMOTE_AGENT_DIR"
    ssh "$REMOTE_HOST" "mkdir -p $REMOTE_AGENT_DIR" || { echo "could not reach $REMOTE_HOST"; restoreAgentVersion; return; }
    scp "$exe" "$sig" "$REMOTE_HOST:$REMOTE_AGENT_DIR/" || { echo "upload of exe/signature failed"; restoreAgentVersion; return; }
    scp "$manifest" "$REMOTE_HOST:$REMOTE_AGENT_DIR/" || { echo "upload of latest.json failed"; restoreAgentVersion; return; }

    # Published: the bump is real now, so drop the rollback backups and keep it.
    rm -f "$AGENT_DIR/Cargo.toml.relbak" "$AGENT_DIR/Cargo.lock.relbak"

    echo
    echo "Published acrally-agent v$version."
    echo "  https://fourleft.io/acrally-agent/latest.json"
    echo "  Agents on older versions will pick it up on their next check."
    echo "  Committing the version bump ($oldVersion -> $version) to git is left to you."
}

runAgentRelease
