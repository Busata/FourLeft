<#
.SYNOPSIS
  Build, sign, and stage an acrally-agent release on Windows.

.DESCRIPTION
  Builds the windows-msvc release exe (features ui,shm), signs it with minisign,
  and writes latest.json into .\dist. Uploading is normally done from the WSL side
  (the `fourleft` CLI -> "Release AC Rally agent"), so this does NOT upload unless
  -Upload is given.

  Prereqs on this machine:
    - Rust with the x86_64-pc-windows-msvc target (rustup default on Windows)
    - minisign on PATH (winget install jedisct1.minisign)
    - your minisign secret key (default %USERPROFILE%\.minisign\acrally-agent.key)

  You'll be prompted for the minisign key password during signing.
#>
param(
    [string]$SecretKey    = "$env:USERPROFILE\.minisign\acrally-agent.key",
    [string]$MinSupported = "0.1.0",
    [string]$Notes        = "",
    [string]$BaseUrl      = "https://fourleft.io/acrally-agent",
    [switch]$Upload,
    [string]$RemoteHost   = "veevi",
    [string]$RemoteDir    = "/mnt/docker-data/acrally-agent"
)

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

# --- package version (first line-anchored `version = "..."` in Cargo.toml) ---
$verLine = Select-String -Path "Cargo.toml" -Pattern '^version\s*=\s*"([^"]+)"' | Select-Object -First 1
if (-not $verLine) { throw "could not find package version in Cargo.toml" }
$version = $verLine.Matches[0].Groups[1].Value
Write-Host "Releasing acrally-agent $version" -ForegroundColor Cyan

# --- build ---
$target = "x86_64-pc-windows-msvc"
cargo build --release --target $target --features "ui,shm"
if ($LASTEXITCODE -ne 0) { throw "cargo build failed" }
$builtExe = "target\$target\release\acrally-agent.exe"
if (-not (Test-Path $builtExe)) { throw "expected exe not found: $builtExe" }

# --- stage under .\dist ---
$dist = Join-Path $PSScriptRoot "dist"
New-Item -ItemType Directory -Force -Path $dist | Out-Null
$exe = Join-Path $dist "acrally-agent-$version.exe"
Copy-Item $builtExe $exe -Force

# --- sign (prompts for the key password) ---
if (-not (Get-Command minisign -ErrorAction SilentlyContinue)) {
    throw "minisign not on PATH (winget install jedisct1.minisign)"
}
if (-not (Test-Path $SecretKey)) { throw "minisign secret key not found: $SecretKey" }
Write-Host "Signing (enter your minisign key password)..." -ForegroundColor Yellow
minisign -S -s $SecretKey -m $exe
if ($LASTEXITCODE -ne 0) { throw "minisign signing failed" }   # writes $exe.minisig

# --- manifest (written last-ish; uploaded last by the caller) ---
$manifest = [ordered]@{
    version       = $version
    url           = "$BaseUrl/acrally-agent-$version.exe"
    min_supported = $MinSupported
    notes         = $Notes
}
$jsonPath = Join-Path $dist "latest.json"
# UTF-8 without BOM (serde_json rejects a BOM).
[System.IO.File]::WriteAllText($jsonPath, ($manifest | ConvertTo-Json -Compress))

Write-Host "Staged in ${dist}:" -ForegroundColor Green
Get-ChildItem $dist -Filter "*$version*" | ForEach-Object { Write-Host "  $($_.Name)" }
Write-Host "  latest.json"

# --- optional upload (normally the WSL side does this; needs the SSH alias here) ---
if ($Upload) {
    ssh $RemoteHost "mkdir -p $RemoteDir"
    scp $exe "$exe.minisig" "${RemoteHost}:$RemoteDir/"
    scp $jsonPath "${RemoteHost}:$RemoteDir/"          # manifest last
    Write-Host "Uploaded to ${RemoteHost}:$RemoteDir" -ForegroundColor Green
}
