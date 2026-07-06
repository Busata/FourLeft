#!/usr/bin/env python3
"""Generate assets/car.ico from the same side-profile car geometry used by
`src/ui.rs` (car_pixels) and `assets/car.svg`. Run when the icon changes:

    python3 assets/generate_icon.py

Produces a multi-resolution Windows .ico (16..256) that the build script
(`build.rs`) embeds into acrally-agent.exe. Needs Pillow (`pip install pillow`).
"""

from pathlib import Path

from PIL import Image

GRID = 64.0
GREEN = (0x4C, 0xC2, 0x6A, 255)
GLASS = (0xDF, 0xF3, 0xE6, 255)
TYRE = (0x22, 0x26, 0x2B, 255)
HUB = (0xB6, 0xBF, 0xCB, 255)
CLEAR = (0, 0, 0, 0)
WHEELS = ((19.0, 47.0), (45.0, 47.0))


def color_at(x: float, y: float):
    color = CLEAR
    # Lower body + cabin trapezoid (narrower at the top).
    lower = 7.0 <= x <= 57.0 and 34.0 <= y <= 47.0
    inset = (34.0 - y) * 0.45
    cabin = 20.0 <= y <= 34.0 and (22.0 + inset) <= x <= (43.0 - inset)
    if lower or cabin:
        color = GREEN
    # Windshield / cabin glass.
    ginset = (32.0 - y) * 0.45
    if 23.0 <= y <= 32.0 and (25.0 + ginset) <= x <= (40.0 - ginset):
        color = GLASS
    # Wheels.
    for cx, cy in WHEELS:
        d = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5
        if d <= 8.5:
            color = TYRE
        if d <= 3.0:
            color = HUB
    return color


def render(n: int) -> Image.Image:
    img = Image.new("RGBA", (n, n), CLEAR)
    px = img.load()
    for py in range(n):
        for pxi in range(n):
            x = (pxi + 0.5) * GRID / n
            y = (py + 0.5) * GRID / n
            px[pxi, py] = color_at(x, y)
    return img


def main() -> None:
    # Render once at high resolution, then let the ICO writer downscale (LANCZOS)
    # to each target size for anti-aliased edges.
    master = render(1024).resize((256, 256), Image.LANCZOS)
    out = Path(__file__).with_name("car.ico")
    sizes = [(16, 16), (24, 24), (32, 32), (48, 48), (64, 64), (128, 128), (256, 256)]
    master.save(out, format="ICO", sizes=sizes)
    print(f"wrote {out} ({', '.join(f'{w}x{h}' for w, h in sizes)})")


if __name__ == "__main__":
    main()
