//! Build script: on a Windows target, embed the app icon and version/product
//! metadata into `acrally-agent.exe` so it looks like a real app in Explorer, the
//! taskbar, and the "unknown publisher" download dialog. A no-op on other targets.

fn main() {
    // `CARGO_CFG_WINDOWS` is set when the *target* OS is Windows (works for a
    // cross-build too), unlike `cfg!(windows)` which reflects the build host.
    if std::env::var_os("CARGO_CFG_WINDOWS").is_none() {
        return;
    }

    println!("cargo:rerun-if-changed=assets/car.ico");

    let mut res = winresource::WindowsResource::new();
    res.set_icon("assets/car.ico");
    // FileVersion/ProductVersion are auto-filled from CARGO_PKG_VERSION.
    res.set("ProductName", "Fourleft.IO - AC Rally Companion");
    res.set("FileDescription", "AC Rally club companion agent");
    res.set("CompanyName", "fourleft.io");
    res.set("OriginalFilename", "acrally-agent.exe");

    if let Err(e) = res.compile() {
        // Don't fail the build over metadata; just warn (e.g. rc/llvm-rc missing).
        println!("cargo:warning=could not embed Windows resources: {e}");
    }
}
