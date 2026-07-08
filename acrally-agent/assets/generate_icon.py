#!/usr/bin/env python3
"""Generate the companion's icon artifacts from the fourleft logo tile.

    python3 assets/generate_icon.py [source.png]

Reads the logo (default: assets/logo.png), and if it still has an opaque white
surround (AI exports bake one in), crops to the dark rounded tile and cuts the
corners transparent with a matching rounded-rect mask. Writes:

  - assets/logo.png      the processed, transparent logo (source of truth)
  - assets/logo.ico      multi-resolution Windows icon (16..256), embedded into
                         acrally-agent.exe by build.rs
  - assets/icon-64.rgba  raw 64x64 RGBA, embedded by src/ui.rs as the window icon

Needs Pillow (`pip install pillow`).
"""

import sys
from pathlib import Path

from PIL import Image, ImageDraw

ASSETS = Path(__file__).resolve().parent
# Luminance below this counts as the tile; above it, as the white surround.
DARK = 120
# Pixels shaved off the tile edge by the mask, hiding the white anti-alias fringe.
EDGE_INSET = 2
SUPERSAMPLE = 4


def strip_surround(img: Image.Image) -> Image.Image:
    """Crop to the dark rounded tile and make everything outside it transparent."""
    img = img.convert("RGBA")
    lum = img.convert("L")
    tile = lum.point(lambda v: 255 if v < DARK else 0)
    bbox = tile.getbbox()
    if bbox is None:
        raise SystemExit("no dark tile found — is this the right image?")
    cropped = img.crop(bbox)
    tile_cropped = tile.crop(bbox)
    w, h = cropped.size

    # Corner radius: on the tile's top row, the dark run starts r pixels in.
    top = tile_cropped.load()
    radius = next((x for x in range(w) if top[x, 0]), 0)

    # Anti-aliased rounded-rect alpha mask, drawn supersampled and inset a touch
    # so the white fringe along the tile edge is cut away with the surround.
    s = SUPERSAMPLE
    mask = Image.new("L", (w * s, h * s), 0)
    inset = EDGE_INSET * s
    ImageDraw.Draw(mask).rounded_rectangle(
        [inset, inset, w * s - 1 - inset, h * s - 1 - inset],
        radius=max(radius - EDGE_INSET, 0) * s,
        fill=255,
    )
    cropped.putalpha(mask.resize((w, h), Image.LANCZOS))
    return cropped


def main() -> None:
    source = Path(sys.argv[1]) if len(sys.argv) > 1 else ASSETS / "logo.png"
    img = Image.open(source)

    # Skip the matte step when the logo already has transparent corners.
    already_cut = img.mode == "RGBA" and img.getpixel((0, 0))[3] == 0
    logo = img.convert("RGBA") if already_cut else strip_surround(img)

    # Pad to a square canvas so the fixed-size icons don't squish the tile.
    w, h = logo.size
    side = max(w, h)
    if (w, h) != (side, side):
        square = Image.new("RGBA", (side, side), (0, 0, 0, 0))
        square.paste(logo, ((side - w) // 2, (side - h) // 2))
        logo = square

    logo.save(ASSETS / "logo.png")

    master = logo.resize((256, 256), Image.LANCZOS)
    sizes = [(16, 16), (24, 24), (32, 32), (48, 48), (64, 64), (128, 128), (256, 256)]
    master.save(ASSETS / "logo.ico", format="ICO", sizes=sizes)

    icon64 = logo.resize((64, 64), Image.LANCZOS)
    (ASSETS / "icon-64.rgba").write_bytes(icon64.tobytes())

    print(f"logo.png {logo.size[0]}x{logo.size[1]}, logo.ico {len(sizes)} sizes, icon-64.rgba")


if __name__ == "__main__":
    main()
