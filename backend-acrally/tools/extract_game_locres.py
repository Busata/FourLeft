#!/usr/bin/env python3
"""Extract localization strings (Game.locres) from AC Rally's pak files.

Recovers the TRACK_* stage/variant display names (e.g. TRACK_ELATIA_FULL_REVERSE
= "Zeli - Elatia") that seed R__seed_stage_variants.sql. Works directly on the
classic pak (pak v11, Oodle-compressed); no full unpack needed.

The Oodle decompressor is not redistributable; --fetch-oodle downloads
liboo2corelinux64.so.9 into ~/.cache/retoc from the same pinned source retoc
(github.com/trumank/retoc) uses, or pass an existing copy with --oodle-lib.

Usage:
  ./extract_game_locres.py --fetch-oodle
  ./extract_game_locres.py --game-dir "/mnt/c/Program Files (x86)/Steam/steamapps/common/Assetto Corsa Rally" \
      --oodle-lib ./liboo2corelinux64.so.9 --locale en --out game_locres_en.json

For IoStore assets (DT_Tracks*, DA_DBMain under /Game/Data/Database), use retoc
itself: `retoc to-legacy --filter <name> <Paks dir> <out dir>`.
"""
import argparse
import ctypes
import json
import struct
import sys
from pathlib import Path

PAK_MAGIC = b"\xe1\x12\x6f\x5a"  # 0x5A6F12E1 little-endian
DEFAULT_GAME_DIR = "/mnt/c/Program Files (x86)/Steam/steamapps/common/Assetto Corsa Rally"
OODLE_LIB_NAME = "liboo2corelinux64.so.9"
# same source + pinned hash retoc itself uses (oodle_loader crate)
OODLE_URL = ("https://github.com/WorkingRobot/OodleUE/raw/refs/heads/main/"
             "Engine/Source/Programs/Shared/EpicGames.Oodle/Sdk/2.9.10/linux/lib/" + OODLE_LIB_NAME)
OODLE_SHA256 = "ed7e98f70be1254a80644efd3ae442ff61f854a2fe9debb0b978b95289884e9c"


def read_fstring(d, o):
    n, = struct.unpack_from("<i", d, o)
    o += 4
    if n < 0:
        s = d[o:o + (-n - 1) * 2].decode("utf-16-le")
        o += -n * 2
    else:
        s = d[o:o + n - 1].decode("latin1")
        o += n
    return s, o


def read_pak_footer(f):
    """Locate the pak footer by scanning the file tail for the magic."""
    f.seek(0, 2)
    fsize = f.tell()
    tail_len = min(fsize, 4096)
    f.seek(fsize - tail_len)
    tail = f.read(tail_len)
    m = tail.rfind(PAK_MAGIC)
    if m < 0:
        raise SystemExit("pak magic not found in file tail — not a pak file?")
    version, idxoff, idxsize = struct.unpack_from("<iqq", tail, m + 4)
    # after Version(4) IndexOffset(8) IndexSize(8) Hash(20) come the 32-byte
    # compression method names, running to EOF
    names_blob = tail[m + 4 + 4 + 8 + 8 + 20:]
    methods = [names_blob[i:i + 32].split(b"\0")[0].decode("latin1")
               for i in range(0, len(names_blob), 32)]
    methods = [n for n in methods if n]
    encrypted = tail[m - 1]
    if encrypted:
        raise SystemExit("pak index is encrypted — this script does not handle AES paks")
    return version, idxoff, idxsize, methods


def parse_index(f, idxoff, idxsize):
    """Parse the pak v11 primary + full directory index.

    Returns (files, enc) where files maps path -> offset into the encoded
    entry blob `enc`.
    """
    f.seek(idxoff)
    data = f.read(idxsize)
    o = 0
    mount, o = read_fstring(data, o)
    num, = struct.unpack_from("<i", data, o); o += 4
    o += 8  # path hash seed
    has_path_hash, = struct.unpack_from("<i", data, o); o += 4
    if has_path_hash:
        o += 16 + 20  # offset+size, hash
    has_full_dir, = struct.unpack_from("<i", data, o); o += 4
    if not has_full_dir:
        raise SystemExit("pak has no full directory index")
    fdo, fds = struct.unpack_from("<qq", data, o); o += 16 + 20
    enc_size, = struct.unpack_from("<i", data, o); o += 4
    enc = data[o:o + enc_size]

    f.seek(fdo)
    fd = f.read(fds)
    o2 = 0
    ndirs, = struct.unpack_from("<i", fd, o2); o2 += 4
    files = {}
    for _ in range(ndirs):
        dname, o2 = read_fstring(fd, o2)
        nfiles, = struct.unpack_from("<i", fd, o2); o2 += 4
        for _ in range(nfiles):
            fname, o2 = read_fstring(fd, o2)
            eo, = struct.unpack_from("<i", fd, o2); o2 += 4
            files[mount + dname + fname] = eo
    return files, enc


def decode_entry(enc, eo):
    """Decode one encoded FPakEntry (pak v11 bit-packed form)."""
    val, = struct.unpack_from("<I", enc, eo); eo += 4
    compidx = (val >> 23) & 0x3F
    if val & (1 << 31):
        offset, = struct.unpack_from("<I", enc, eo); eo += 4
    else:
        offset, = struct.unpack_from("<q", enc, eo); eo += 8
    if val & (1 << 30):
        usize, = struct.unpack_from("<I", enc, eo); eo += 4
    else:
        usize, = struct.unpack_from("<q", enc, eo); eo += 8
    if compidx:
        if val & (1 << 29):
            size, = struct.unpack_from("<I", enc, eo); eo += 4
        else:
            size, = struct.unpack_from("<q", enc, eo); eo += 8
    else:
        size = usize
    encrypted = (val >> 22) & 1
    nblocks = (val >> 6) & 0xFFFF
    if nblocks > 0 and (val & 0x3F) == 0x3F:
        blocksize, = struct.unpack_from("<I", enc, eo); eo += 4
    else:
        blocksize = (val & 0x3F) << 11
    blocks = []
    if nblocks == 1 and not encrypted:
        blocks = [(0, size)]
    elif nblocks > 0:
        cur = 0
        for _ in range(nblocks):
            bsz, = struct.unpack_from("<I", enc, eo); eo += 4
            blocks.append((cur, bsz))
            cur += bsz
    return dict(offset=offset, size=size, usize=usize, compidx=compidx,
                encrypted=encrypted, nblocks=nblocks, blocksize=blocksize,
                blocks=blocks)


def load_oodle(lib_path):
    oo = ctypes.CDLL(str(lib_path))
    oo.OodleLZ_Decompress.restype = ctypes.c_ssize_t
    oo.OodleLZ_Decompress.argtypes = [
        ctypes.c_char_p, ctypes.c_ssize_t, ctypes.c_char_p, ctypes.c_ssize_t,
        ctypes.c_int, ctypes.c_int, ctypes.c_int, ctypes.c_void_p,
        ctypes.c_ssize_t, ctypes.c_void_p, ctypes.c_void_p, ctypes.c_void_p,
        ctypes.c_ssize_t, ctypes.c_int]
    return oo


def read_entry_data(f, entry, methods, oodle):
    if entry["encrypted"]:
        raise SystemExit("entry is encrypted — not supported")
    if entry["compidx"] == 0:
        hdr = 8 + 8 + 8 + 4 + 20 + 1 + 4
        f.seek(entry["offset"] + hdr)
        return f.read(entry["usize"])
    method = methods[entry["compidx"] - 1]
    hdr = 8 + 8 + 8 + 4 + 20 + 4 + entry["nblocks"] * 16 + 1 + 4
    data = b""
    remaining = entry["usize"]
    for rel, csz in entry["blocks"]:
        f.seek(entry["offset"] + hdr + rel)
        comp = f.read(csz)
        want = min(entry["blocksize"], remaining)
        if method.startswith("Oodle"):
            if oodle is None:
                raise SystemExit(f"entry uses {method}; pass --oodle-lib or --fetch-oodle")
            dst = ctypes.create_string_buffer(want)
            # fuzzSafe=1, checkCRC=0, verbosity=0, ..., threadPhase=3
            n = oodle.OodleLZ_Decompress(comp, len(comp), dst, want,
                                         1, 0, 0, None, 0, None, None, None, 0, 3)
            if n != want:
                raise SystemExit(f"Oodle decompress failed: got {n}, wanted {want}")
            data += dst.raw
        elif method == "Zlib":
            import zlib
            data += zlib.decompress(comp)
        else:
            raise SystemExit(f"unsupported compression method {method!r}")
        remaining -= want
    return data


def parse_locres(d):
    def rstr(o):
        n, = struct.unpack_from("<i", d, o)
        o += 4
        if n < 0:
            s = d[o:o + (-n - 1) * 2].decode("utf-16-le")
            o += -n * 2
        else:
            s = d[o:o + n - 1].decode("utf-8", "replace")
            o += n
        return s, o

    o = 16  # magic guid
    ver = d[o]; o += 1
    strsoff, = struct.unpack_from("<q", d, o); o += 8
    o += 4  # total entry count
    nns, = struct.unpack_from("<I", d, o); o += 4

    so = strsoff
    cnt, = struct.unpack_from("<I", d, so); so += 4
    strings = []
    for _ in range(cnt):
        s, so = rstr(so)
        if ver >= 3:
            so += 4  # refcount
        strings.append(s)

    out = {}
    for _ in range(nns):
        o += 4  # namespace hash
        _ns, o = rstr(o)
        nk, = struct.unpack_from("<I", d, o); o += 4
        for _ in range(nk):
            o += 4  # key hash
            key, o = rstr(o)
            o += 4  # source string hash
            idx, = struct.unpack_from("<I", d, o); o += 4
            out[key] = strings[idx]
    return out


def fetch_oodle(cache_dir):
    lib = cache_dir / OODLE_LIB_NAME
    if lib.exists():
        return lib
    import hashlib
    import urllib.request
    print(f"downloading {OODLE_URL} ...", file=sys.stderr)
    cache_dir.mkdir(parents=True, exist_ok=True)
    with urllib.request.urlopen(OODLE_URL) as resp:
        blob = resp.read()
    digest = hashlib.sha256(blob).hexdigest()
    if digest != OODLE_SHA256:
        raise SystemExit(f"Oodle lib hash mismatch: expected {OODLE_SHA256}, got {digest}")
    lib.write_bytes(blob)
    return lib


def main():
    ap = argparse.ArgumentParser(description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--game-dir", default=DEFAULT_GAME_DIR)
    ap.add_argument("--pak", help="explicit pak path (default: <game-dir>/acr/Content/Paks/pakchunk0-Windows.pak)")
    ap.add_argument("--locale", default="en")
    ap.add_argument("--oodle-lib", help=f"path to an existing {OODLE_LIB_NAME}")
    ap.add_argument("--fetch-oodle", action="store_true",
                    help=f"download {OODLE_LIB_NAME} to ~/.cache/retoc and use it")
    ap.add_argument("--out", default="game_locres.json")
    ap.add_argument("--filter", default=None, help="only emit keys containing this substring (e.g. TRACK_)")
    args = ap.parse_args()

    pak_path = Path(args.pak) if args.pak else Path(args.game_dir) / "acr/Content/Paks/pakchunk0-Windows.pak"
    if not pak_path.exists():
        raise SystemExit(f"pak not found: {pak_path}")

    oodle = None
    if args.fetch_oodle:
        oodle = load_oodle(fetch_oodle(Path.home() / ".cache/retoc"))
    elif args.oodle_lib:
        oodle = load_oodle(args.oodle_lib)

    with open(pak_path, "rb") as f:
        version, idxoff, idxsize, methods = read_pak_footer(f)
        print(f"pak v{version}, index @ {idxoff} ({idxsize} bytes), compression: {methods}", file=sys.stderr)
        files, enc = parse_index(f, idxoff, idxsize)
        wanted = f"Localization/Game/{args.locale}/Game.locres"
        matches = [p for p in files if p.endswith(wanted)]
        if not matches:
            available = sorted(p for p in files if p.lower().endswith(".locres"))
            raise SystemExit(f"{wanted} not in pak; locres files present:\n" + "\n".join(available))
        entry = decode_entry(enc, files[matches[0]])
        print(f"{matches[0]}: {entry['size']} -> {entry['usize']} bytes, {entry['nblocks']} blocks", file=sys.stderr)
        data = read_entry_data(f, entry, methods, oodle)

    out = parse_locres(data)
    if args.filter:
        out = {k: v for k, v in out.items() if args.filter in k}
    with open(args.out, "w") as fh:
        json.dump(out, fh, indent=1, ensure_ascii=False, sort_keys=True)
    tracks = sum(1 for k in out if k.startswith("TRACK_"))
    print(f"wrote {len(out)} entries ({tracks} TRACK_*) to {args.out}", file=sys.stderr)


if __name__ == "__main__":
    main()
