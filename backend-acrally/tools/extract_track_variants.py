#!/usr/bin/env python3
"""Extract the variant catalogue (raw IDs, in-game order, display names) from AC Rally.

Companion to extract_game_locres.py: that script yields the TRACK_* display
strings; this one recovers the per-stage variant ORDER from the
DT_TracksVariants data table (the row order defines the in-game "Variant N"
numbering) and joins the two into the seed data behind
R__seed_stage_variants.sql.

Needs the retoc CLI (github.com/trumank/retoc) to convert the IoStore asset to
legacy uasset/uexp first — pass --retoc, or --fetch-retoc to download and cache
it. The uasset parse itself is heuristic (name-map scan + FName occurrence
ordering) but verified against the 2026-07 build.

Usage:
  ./extract_game_locres.py --fetch-oodle --out game_locres_en.json
  ./extract_track_variants.py --fetch-retoc --locres game_locres_en.json --out variant_seed.json
"""
import argparse
import json
import re
import struct
import subprocess
import sys
from pathlib import Path

DEFAULT_GAME_DIR = "/mnt/c/Program Files (x86)/Steam/steamapps/common/Assetto Corsa Rally"
RETOC_URL = "https://github.com/trumank/retoc/releases/download/v0.1.5/retoc_cli-x86_64-unknown-linux-gnu.tar.xz"
DT_ASSET = "acr/Content/Data/Database/Main/TrackSelection/DT_TracksVariants"
# variant ID: <EnvironmentPrefix><Layout><Direction>, no underscore
# (row names in the same table use <Prefix>_<Layout><Direction>)
ID_RE = re.compile(r"^[A-Za-z0-9]+?((?:Full|Cut\d+|Short\d+)(?:Forward|Reverse))$")


def fetch_retoc(cache_dir):
    retoc = cache_dir / "retoc"
    if retoc.exists():
        return retoc
    import io
    import tarfile
    import urllib.request
    print(f"downloading {RETOC_URL} ...", file=sys.stderr)
    cache_dir.mkdir(parents=True, exist_ok=True)
    with urllib.request.urlopen(RETOC_URL) as resp:
        buf = io.BytesIO(resp.read())
    with tarfile.open(fileobj=buf, mode="r:xz") as tar:
        for member in tar.getmembers():
            if member.name.endswith("/retoc"):
                member.name = "retoc"
                tar.extract(member, cache_dir)
                retoc.chmod(0o755)
                return retoc
    raise SystemExit("retoc binary not found in release tarball")


def read_name_map(ua):
    """Locate and read the uasset name map by scanning the summary for a
    plausible (count, offset) pair; detects whether entries carry hashes."""
    def attempt(off, cnt):
        names = []
        o = off
        has_hash = None
        for i in range(cnt):
            if o + 4 > len(ua):
                return None
            n, = struct.unpack_from("<i", ua, o)
            if n > 0:
                if n > 200 or o + 4 + n > len(ua) or ua[o + 4 + n - 1] != 0:
                    return None
                s = ua[o + 4:o + 4 + n - 1].decode("latin1")
                o += 4 + n
            elif n < 0:
                if -n > 200 or o + 4 - 2 * n > len(ua):
                    return None
                s = ua[o + 4:o + 4 - 2 * n - 2].decode("utf-16-le", "replace")
                o += 4 - 2 * n
            else:
                return None
            names.append(s)
            if has_hash is None:
                # peek: if the next int32 doesn't look like a string length,
                # entries carry a trailing 4-byte hash
                if o + 4 <= len(ua):
                    n2, = struct.unpack_from("<i", ua, o)
                    has_hash = not (0 < n2 <= 200 and o + 4 + n2 <= len(ua)
                                    and ua[o + 4 + n2 - 1] == 0)
                else:
                    has_hash = True
            if has_hash:
                o += 4
        return names

    for o in range(0, 300):
        if o + 8 > len(ua):
            break
        cnt, off = struct.unpack_from("<ii", ua, o)
        if 10 < cnt < 20000 and 100 < off < len(ua) - 8:
            names = attempt(off, cnt)
            # a full clean parse of cnt consecutive strings is the validation;
            # (the 2026-07 build also had a literal "None" entry, later ones don't)
            if names:
                return names
    raise SystemExit("could not locate uasset name map")


def collect_events(names, ux):
    """Ordered (position, kind, name) events in the export data: variant-ID
    FName occurrences and TRACK_* loc-key strings."""
    idx = {n: i for i, n in enumerate(names)}
    events = []
    for name in names:
        if not ID_RE.match(name):
            continue
        pat = struct.pack("<II", idx[name], 0)  # FName (index, number=0)
        p = ux.find(pat)
        while p != -1:
            events.append((p, "ID", name))
            p = ux.find(pat, p + 1)
    for m in re.finditer(rb"TRACK_[A-Z0-9_]+", ux):
        events.append((m.start(), "LOC", m.group().decode()))
    events.sort()
    return events


def build_variants(events, locres):
    """Group loc keys under the preceding variant ID; a row's ID occurs at both
    its start and end, so consecutive duplicate blocks are merged. Variant
    numbers follow first-appearance (row) order per environment prefix."""
    rows = []
    cur = None
    for _, kind, name in events:
        if kind == "ID":
            if cur is None or cur[0] != name:
                cur = [name, []]
                rows.append(cur)
        elif kind == "LOC" and cur and not name.endswith("_SHORT"):
            if name not in cur[1]:
                cur[1].append(name)
    merged, order = {}, []
    for rid, keys in rows:
        if rid not in merged:
            merged[rid] = keys
            order.append(rid)
        else:
            merged[rid].extend(k for k in keys if k not in merged[rid])
    # ID-shaped names without loc keys are cross-references to OTHER tables
    # (AITrackVariantsCarsData rows — with Kunos's own "Wales"/"MonteCarlos"
    # typos — and TracksTextures rows), not variant rows; drop them before
    # numbering so they can't shift the per-stage variant order
    dropped = [rid for rid in order if not merged[rid]]
    if dropped:
        print(f"ignoring {len(dropped)} reference-only ids (no loc keys): "
              + ", ".join(dropped), file=sys.stderr)
    order = [rid for rid in order if merged[rid]]
    counters = {}
    out = []
    for rid in order:
        prefix = rid[:ID_RE.match(rid).start(1)]
        counters[prefix] = counters.get(prefix, 0) + 1
        keys = merged[rid]
        name = locres.get(keys[0], "???").strip() if keys else "???"
        out.append(dict(raw_name=rid, prefix=prefix, variant=counters[prefix],
                        name=name, display_name=f"{name} (Variant {counters[prefix]})",
                        loc_keys=keys))
    return out


def main():
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--game-dir", default=DEFAULT_GAME_DIR)
    ap.add_argument("--locres", required=True,
                    help="JSON from extract_game_locres.py (display strings)")
    ap.add_argument("--retoc", help="path to the retoc binary")
    ap.add_argument("--fetch-retoc", action="store_true",
                    help="download retoc to ~/.cache/retoc and use it")
    ap.add_argument("--workdir", help="where retoc extracts the asset (default: ~/.cache/retoc/out)")
    ap.add_argument("--out", default="variant_seed.json")
    args = ap.parse_args()

    if args.fetch_retoc:
        retoc = fetch_retoc(Path.home() / ".cache/retoc")
    elif args.retoc:
        retoc = Path(args.retoc)
    else:
        raise SystemExit("pass --retoc or --fetch-retoc")

    workdir = Path(args.workdir) if args.workdir else Path.home() / ".cache/retoc/out"
    workdir.mkdir(parents=True, exist_ok=True)
    paks = Path(args.game_dir) / "acr/Content/Paks"
    subprocess.run([str(retoc), "to-legacy", "--filter", "DT_Tracks", "--no-shaders",
                    str(paks), str(workdir)], check=True)

    ua = (workdir / DT_ASSET).with_suffix(".uasset").read_bytes()
    ux = (workdir / DT_ASSET).with_suffix(".uexp").read_bytes()
    locres = json.load(open(args.locres))

    names = read_name_map(ua)
    variants = build_variants(collect_events(names, ux), locres)

    for v in variants:
        print(f"{v['raw_name']:40s} V{v['variant']}  {v['name']:42s} {v['loc_keys']}")
    print(f"{len(variants)} variants across "
          f"{len({v['prefix'] for v in variants})} environments", file=sys.stderr)
    with open(args.out, "w") as fh:
        json.dump(variants, fh, ensure_ascii=False, indent=1)
    print(f"wrote {args.out}", file=sys.stderr)


if __name__ == "__main__":
    main()
