#!/usr/bin/env python3
"""Generate R__seed_track_aliases.sql from extracted game data.

Live telemetry reports the track as "<localized location> <localized variant
label>" written into a fixed [u16; 33] shared-memory field — so truncated to
32 UTF-16 code units. The label is the TRACK_*_SHORT locres key (whose value
often equals the full variant name), and any key missing from the client's
locale falls back to English per key. Older agents report the raw variant key
itself. This script derives every string the game can produce that way — all
locales, short and full label forms, truncated — plus the raw-key self-maps,
and emits an upsert seed that only ever fills unassigned aliases (admin
assignments are never overwritten).

Strings a community translation mod produces (e.g. Portuguese "Grécia Zeli")
cannot be derived and still need manual assignment in the admin UI.

Usage (after extract_game_locres.py for each locale + extract_track_variants.py):
  ./generate_track_alias_seed.py --variants variant_seed.json --locres-dir . \
      --out ../src/main/resources/db/migration/R__seed_track_aliases.sql
"""
import argparse
import json
import sys
from pathlib import Path

LOCALES = ["de", "en", "es", "fr", "it", "zh-Hans"]
TRACK_FIELD_LEN = 32  # UTF-16 code units, [u16; 33] minus the terminator
# stage prefix (variant raw-key prefix) -> TRACK_LOCATION_* key suffix
LOCATION_KEY = {
    "AlsaceS2Munster": "ALSACE", "AlsaceS4Saverne": "ALSACE",
    "GreeceS3Elatia": "GRECE", "GreeceS4Loutraki": "GRECE",  # GRECE sic
    "LivignoTestTrack01": "LIVIGNO",
    "MonteCarloS1Bollene": "MONTECARLO", "MonteCarloS2Sisteron": "MONTECARLO",
    "WelesS3HafrenNorth": "WALES", "WelesS4HafrenSouth": "WALES",
}


def main():
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--variants", required=True, help="variant_seed.json from extract_track_variants.py")
    ap.add_argument("--locres-dir", required=True,
                    help="directory holding game_locres_<locale>.json for: " + ", ".join(LOCALES))
    ap.add_argument("--out", required=True)
    args = ap.parse_args()

    variants = json.load(open(args.variants))
    locres = {loc: json.load(open(Path(args.locres_dir) / f"game_locres_{loc}.json"))
              for loc in LOCALES}
    en = locres["en"]

    def lookup(loc, key):  # per-key fallback to English, like the game
        return locres[loc].get(key) or en.get(key)

    # composed string -> set of variant raw_names it could mean
    composed = {}
    for v in variants:
        key = v["loc_keys"][0]
        unknown = [p for p in [v["prefix"]] if p not in LOCATION_KEY]
        if unknown:
            print(f"no location mapping for prefix {unknown[0]} — extend LOCATION_KEY", file=sys.stderr)
            continue
        for loc in LOCALES:
            location = lookup(loc, f"TRACK_LOCATION_{LOCATION_KEY[v['prefix']]}")
            # the game uses the _SHORT label; emit the full name too in case a
            # context uses it — collisions are dropped below either way
            for label in (lookup(loc, key + "_SHORT") or lookup(loc, key), lookup(loc, key)):
                if not location or not label:
                    continue
                alias = f"{location} {label}"[:TRACK_FIELD_LEN]
                composed.setdefault(alias, set()).add(v["raw_name"])

    ambiguous = sorted(a for a, targets in composed.items() if len(targets) > 1)
    if ambiguous:
        print(f"dropping {len(ambiguous)} ambiguous strings (map to >1 variant): "
              + "; ".join(ambiguous), file=sys.stderr)
    rows = [(a, next(iter(t))) for a, t in composed.items() if len(t) == 1]
    rows += [(v["raw_name"], v["raw_name"]) for v in variants]  # old-agent self-maps
    rows.sort()

    def q(s):
        return s.replace("'", "''")

    header = f"""-- Repeatable seed of track aliases derivable from the game files. GENERATED —
-- do not edit by hand; regenerate with tools/generate_track_alias_seed.py
-- (see its docstring for the full recipe) and commit the result.
--
-- Live telemetry writes "<localized location> <localized variant label>" into a
-- 32-UTF-16-unit field (hence the truncated entries); labels come from the
-- TRACK_*_SHORT locres keys with per-key English fallback, so most strings mix
-- languages. Older agents report the raw variant key itself. Community
-- translation mods produce strings not covered here — those stay for admin
-- assignment, as do the {len(ambiguous)} composition(s) dropped as ambiguous.
--
-- Upsert policy: insert unknown aliases pre-assigned, fill variant_id on
-- existing unassigned rows, never touch an alias an admin already assigned.

INSERT INTO track_alias (id, raw_name, variant_id, created_at)
SELECT gen_random_uuid(), v.alias, var.id, now()
FROM (VALUES
"""
    body = ",\n".join(f"    ('{q(alias)}', '{raw}')" for alias, raw in rows)
    footer = """
) AS v(alias, variant_raw)
JOIN variant var ON var.raw_name = v.variant_raw
ON CONFLICT (raw_name) DO UPDATE SET
    variant_id = EXCLUDED.variant_id,
    updated_at = now()
WHERE track_alias.variant_id IS NULL;
"""
    Path(args.out).write_text(header + body + footer)
    print(f"wrote {len(rows)} alias rows ({len(rows) - len(variants)} composed, "
          f"{len(variants)} raw self-maps) to {args.out}", file=sys.stderr)


if __name__ == "__main__":
    main()
