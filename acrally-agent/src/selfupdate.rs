//! Self-update: poll a signed version manifest on fourleft.io and, when a newer
//! **signed** build is available, atomically replace this executable and relaunch.
//!
//! The manifest lives at a stable URL and names the latest release:
//!
//! ```json
//! { "version": "0.2.0",
//!   "url": "https://fourleft.io/acrally-agent/acrally-agent-0.2.0.exe",
//!   "min_supported": "0.1.0",
//!   "notes": "…" }
//! ```
//!
//! Each release exe is signed with minisign; the detached signature is served as
//! `<url>.minisig`. The agent verifies that signature against a public key baked
//! into the binary *before* swapping — so a compromised or MITM'd static host
//! can't push a trojaned agent that forges results. minisign signs the exact
//! bytes, so a separate content hash would be redundant.
//!
//! The swap uses the `self-replace` crate (rename-the-running-exe trick on
//! Windows), then we relaunch the replaced exe and exit.

use std::io::Read;
use std::time::Duration;

use anyhow::{anyhow, bail, Context, Result};
use serde::Deserialize;

/// Where the latest-release pointer lives. Overridable via `ACRALLY_UPDATE_URL`
/// for testing against a local/staging manifest.
const DEFAULT_MANIFEST_URL: &str = "https://fourleft.io/acrally-agent/latest.json";

/// minisign public key that release signatures are checked against. Generate a
/// keypair once with `minisign -G` (or `rsign generate`), paste the PUBLIC key's
/// base64 body here, and keep the secret key offline. While this is empty the
/// agent refuses to self-update (it will still *report* that one is available).
const UPDATE_PUBLIC_KEY: &str = "RWTtZdnLSToFo18e2jDZVd71R9BJldP/nCfjDUmVDhU2res1wsXRiS1S";

#[derive(Debug, Deserialize)]
struct Manifest {
    version: String,
    url: String,
    #[serde(default)]
    notes: String,
    /// Oldest version the backend still accepts; informational for now.
    #[serde(default)]
    #[allow(dead_code)]
    min_supported: Option<String>,
}

/// A release newer than what's running.
pub struct Available {
    pub version: semver::Version,
    manifest: Manifest,
}

fn manifest_url() -> String {
    std::env::var("ACRALLY_UPDATE_URL").unwrap_or_else(|_| DEFAULT_MANIFEST_URL.to_string())
}

fn http() -> ureq::Agent {
    ureq::AgentBuilder::new()
        .timeout(Duration::from_secs(15))
        .build()
}

fn current_version() -> semver::Version {
    semver::Version::parse(env!("CARGO_PKG_VERSION")).expect("crate version is valid semver")
}

/// Fetch the manifest and report a newer release, if any. Network/parse failures
/// are surfaced as errors so callers can stay quiet on a best-effort check.
pub fn check() -> Result<Option<Available>> {
    let agent = http();
    let manifest: Manifest = agent
        .get(&manifest_url())
        .call()
        .context("could not reach the update server")?
        .into_json()
        .context("malformed update manifest")?;
    let latest = semver::Version::parse(&manifest.version)
        .with_context(|| format!("manifest version '{}' is not semver", manifest.version))?;
    if latest > current_version() {
        Ok(Some(Available { version: latest, manifest }))
    } else {
        Ok(None)
    }
}

/// CLI entry (`acrally-agent update`): check, and if newer, download + verify +
/// replace this exe and relaunch. Prints progress.
pub fn run_update() -> Result<()> {
    match check()? {
        None => {
            println!("acrally-agent {} is up to date.", env!("CARGO_PKG_VERSION"));
            Ok(())
        }
        Some(avail) => {
            println!(
                "Updating acrally-agent {} -> {}…",
                env!("CARGO_PKG_VERSION"),
                avail.version
            );
            if !avail.manifest.notes.is_empty() {
                println!("  {}", avail.manifest.notes);
            }
            apply(&avail.manifest)
        }
    }
}

/// Download, verify, swap, relaunch. Does not return on success (the process
/// re-execs the new binary).
fn apply(m: &Manifest) -> Result<()> {
    if UPDATE_PUBLIC_KEY.is_empty() {
        bail!(
            "no update public key is compiled in, so the new build can't be verified — \
             refusing to self-update. Download the latest exe manually."
        );
    }
    let agent = http();

    // Download the new exe into memory.
    let mut bytes = Vec::new();
    agent
        .get(&m.url)
        .call()
        .context("could not download the update")?
        .into_reader()
        .read_to_end(&mut bytes)
        .context("update download was interrupted")?;

    // Download and verify the detached minisign signature over those exact bytes.
    let sig_text = agent
        .get(&format!("{}.minisig", m.url))
        .call()
        .context("could not download the update signature")?
        .into_string()
        .context("malformed signature file")?;
    verify(&bytes, &sig_text).context("update signature did not verify — aborting update")?;

    // Stage to a temp file, then let self-replace swap it in for the running exe.
    let tmp = std::env::temp_dir().join(format!("acrally-agent-update-{}", m.version));
    std::fs::write(&tmp, &bytes).context("could not write the staged update")?;
    self_replace::self_replace(&tmp).context("could not replace the running executable")?;
    let _ = std::fs::remove_file(&tmp);

    // Relaunch the now-replaced executable and exit this (old) process.
    let exe = std::env::current_exe().context("could not find our own path to relaunch")?;
    std::process::Command::new(exe)
        .spawn()
        .context("updated, but could not relaunch — start acrally-agent again")?;
    println!("Updated to {}. Restarting…", m.version);
    std::process::exit(0);
}

fn verify(data: &[u8], sig_text: &str) -> Result<()> {
    verify_with_key(UPDATE_PUBLIC_KEY, data, sig_text)
}

/// Verify `sig_text` (a detached minisign signature) over `data` against the given
/// base64 public key. Split out from [`verify`] so it can be exercised in tests
/// with a throwaway key. `allow_legacy = false` requires a prehashed signature.
fn verify_with_key(public_key_b64: &str, data: &[u8], sig_text: &str) -> Result<()> {
    use minisign_verify::{PublicKey, Signature};
    let pk = PublicKey::from_base64(public_key_b64)
        .map_err(|e| anyhow!("bad public key: {e}"))?;
    let sig = Signature::decode(sig_text).map_err(|e| anyhow!("bad signature format: {e}"))?;
    pk.verify(data, &sig, false)
        .map_err(|e| anyhow!("signature verification failed: {e}"))?;
    Ok(())
}

/// Best-effort, non-blocking startup check: prints a one-line nudge if a newer
/// build exists. Never blocks the pipeline; stays silent on any error.
/// Only wired up on Windows (the distributed target).
#[cfg_attr(not(windows), allow(dead_code))]
pub fn spawn_background_check() {
    std::thread::spawn(|| {
        if let Ok(Some(avail)) = check() {
            eprintln!(
                "note: acrally-agent {} is available (you have {}). \
                 Run `acrally-agent update` to upgrade.",
                avail.version,
                env!("CARGO_PKG_VERSION"),
            );
        }
    });
}

// ---- UI-driven update (tray app) ----

/// Live update state published to the tray UI, mirroring pairing's `Phase`.
#[cfg(feature = "ui")]
#[derive(Clone, Default)]
pub enum UpdateState {
    /// Nothing checked yet.
    #[default]
    Idle,
    /// Contacting the update server.
    Checking,
    /// Running the newest published build.
    UpToDate,
    /// A newer signed build is available.
    Available { version: String, notes: String },
    /// Downloading + verifying; the app relaunches on success.
    Downloading { version: String },
    /// Something went wrong; carries a user-facing message.
    Failed(String),
}

/// Background: check once and publish the outcome into `state`.
#[cfg(feature = "ui")]
pub fn drive_check(state: std::sync::Arc<std::sync::Mutex<UpdateState>>) {
    let set = |s: UpdateState| {
        if let Ok(mut g) = state.lock() {
            *g = s;
        }
    };
    set(UpdateState::Checking);
    match check() {
        Ok(Some(avail)) => set(UpdateState::Available {
            version: avail.version.to_string(),
            notes: avail.manifest.notes.clone(),
        }),
        Ok(None) => set(UpdateState::UpToDate),
        Err(e) => set(UpdateState::Failed(e.to_string())),
    }
}

/// Background: apply the latest update (download + verify + replace + relaunch),
/// publishing progress into `state`. On success the process re-execs and never
/// returns here; on failure the state carries the reason.
#[cfg(feature = "ui")]
pub fn drive_apply(state: std::sync::Arc<std::sync::Mutex<UpdateState>>) {
    let set = |s: UpdateState| {
        if let Ok(mut g) = state.lock() {
            *g = s;
        }
    };
    match check() {
        Ok(Some(avail)) => {
            set(UpdateState::Downloading {
                version: avail.version.to_string(),
            });
            if let Err(e) = apply(&avail.manifest) {
                set(UpdateState::Failed(e.to_string()));
            }
        }
        Ok(None) => set(UpdateState::UpToDate),
        Err(e) => set(UpdateState::Failed(e.to_string())),
    }
}

#[cfg(test)]
mod tests {
    use super::verify_with_key;

    // A throwaway keypair (generated with rsign2) and its signature over TEST_DATA.
    // NOT the production `UPDATE_PUBLIC_KEY` — this only exercises the minisign
    // verification wiring, so it stays valid regardless of the real release key.
    const TEST_PUBLIC_KEY: &str = "RWTsYWHhtIp5XjbgxzOfZw838SJmoYDGAVKtGcuxHsAJGkZJtKrsQx07";
    const TEST_DATA: &[u8] = b"acrally-agent test payload\n";
    const TEST_SIG: &str = concat!(
        "untrusted comment: signature from rsign secret key\n",
        "RUTsYWHhtIp5XhU7LEiFPLq6lfHBb0LM1p7uLXMUdMSjmeyPkh2gSq0/wiHfaG9VLejMP78n0+lfOzNZvl9UGssiZEKgKaZJ7Ao=\n",
        "trusted comment: timestamp:1783357453\tfile:blob.bin\tprehashed\n",
        "qiL3DRhJ+Dme5Lzrx/2bR8KBYVNEt/iXODhmHPXy835YLlyxCCOH6C4+oS+fko+oH1SpuoCzNjyRGxOVMc9cCQ==\n",
    );

    #[test]
    fn accepts_a_valid_minisign_signature() {
        verify_with_key(TEST_PUBLIC_KEY, TEST_DATA, TEST_SIG)
            .expect("a valid prehashed minisign signature should verify");
    }

    #[test]
    fn rejects_tampered_data() {
        let mut tampered = TEST_DATA.to_vec();
        tampered[0] ^= 0x01;
        assert!(verify_with_key(TEST_PUBLIC_KEY, &tampered, TEST_SIG).is_err());
    }

    #[test]
    fn rejects_wrong_public_key() {
        // Flip a char in the key body so it no longer matches the signer.
        let mut wrong = TEST_PUBLIC_KEY.to_string();
        wrong.replace_range(10..11, "A");
        assert!(verify_with_key(&wrong, TEST_DATA, TEST_SIG).is_err());
    }

    // Catches a paste error in the compiled-in release key (wrong line copied,
    // truncated, etc.). Skipped only if updates are intentionally disabled (empty).
    #[test]
    fn production_public_key_parses() {
        if !super::UPDATE_PUBLIC_KEY.is_empty() {
            minisign_verify::PublicKey::from_base64(super::UPDATE_PUBLIC_KEY)
                .expect("UPDATE_PUBLIC_KEY is not a valid minisign public key");
        }
    }
}
