#!/usr/bin/env python3
"""Local dev server for acrally-agent.

Implements the four endpoints the agent calls, prints every request, keeps a
leaderboard in memory (persisted to dev-server-data.json), and serves a live
web view at http://127.0.0.1:8799/.

No dependencies — just: python3 dev-server.py   (optional: PORT env var)
Point the agent at it with  api_base = "http://127.0.0.1:8799"  in config.toml.
"""
import json
import os
import sys
import threading
from datetime import datetime
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

PORT = int(os.environ.get("PORT", "8799"))
DATA_FILE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "dev-server-data.json")
LOCK = threading.Lock()

# session_id -> {session..., heartbeats: int, last_hb: {...}}
SESSIONS = {}
# list of result dicts
RESULTS = []
_seq = 0


def load():
    global RESULTS
    try:
        with open(DATA_FILE) as f:
            RESULTS = json.load(f).get("results", [])
    except Exception:
        RESULTS = []


def save():
    try:
        with open(DATA_FILE, "w") as f:
            json.dump({"results": RESULTS}, f, indent=2)
    except Exception as e:
        print("  (could not persist:", e, ")")


def fmt_ms(ms):
    ms = int(ms or 0)
    return f"{ms // 60000}:{(ms % 60000) / 1000:06.3f}"


def now():
    return datetime.now().strftime("%H:%M:%S")


class Handler(BaseHTTPRequestHandler):
    def log_message(self, *a):
        pass  # we do our own logging

    def _json(self, code, obj):
        body = json.dumps(obj).encode()
        self.send_response(code)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def _html(self, body):
        b = body.encode()
        self.send_response(200)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.send_header("Content-Length", str(len(b)))
        self.end_headers()
        self.wfile.write(b)

    def do_GET(self):
        if self.path.split("?")[0] in ("/", "/index.html"):
            self._html(render_page())
        elif self.path.startswith("/state"):
            self._json(200, {"sessions": SESSIONS, "results": RESULTS})
        else:
            self._json(404, {"error": "not found"})

    def do_POST(self):
        n = int(self.headers.get("Content-Length", 0))
        raw = self.rfile.read(n) if n else b"{}"
        try:
            d = json.loads(raw or b"{}")
        except Exception:
            d = {}
        parts = [p for p in self.path.split("/") if p]

        with LOCK:
            # POST /sessions
            if parts == ["sessions"]:
                global _seq
                _seq += 1
                sid = f"srv-{_seq}"
                SESSIONS[sid] = {**d, "id": sid, "heartbeats": 0, "opened": now(), "state": "live"}
                print(f"[{now()}] START   {sid}  {d.get('driver')} — {d.get('car')} @ {d.get('stage')}")
                return self._json(200, {"session_id": sid})

            # POST /sessions/{id}/{action}
            if len(parts) == 3 and parts[0] == "sessions":
                sid, action = parts[1], parts[2]
                s = SESSIONS.get(sid)
                if action == "heartbeat":
                    if s:
                        s["heartbeats"] += 1
                        s["last_hb"] = d
                    spd = d.get("speed_kmh", 0) or 0
                    print(f"[{now()}]   HB    {sid}  {spd:5.0f} km/h  g{d.get('gear')}  {d.get('rpm')} rpm  "
                          f"cur={fmt_ms(d.get('current_ms'))}  dist={d.get('distance_m',0):.0f}m")
                    return self._json(200, {"ok": True})
                if action == "result":
                    if s:
                        s["state"] = "finished"
                    entry = {**d, "session_id": sid, "received": now()}
                    # de-dupe by timestamp_ticks
                    if not any(r.get("timestamp_ticks") == d.get("timestamp_ticks") for r in RESULTS):
                        RESULTS.append(entry)
                        save()
                    print(f"[{now()}] RESULT  {sid}  {d.get('car')} @ {d.get('stage')}  "
                          f"TOTAL {fmt_ms(d.get('total_ms'))}  (raw {fmt_ms(d.get('raw_ms'))} + "
                          f"pen {int(d.get('penalty_ms',0))//1000}s)")
                    return self._json(200, {"ok": True})
                if action == "abort":
                    if s:
                        s["state"] = "aborted"
                        s["reason"] = d.get("reason")
                    print(f"[{now()}] ABORT   {sid}  reason={d.get('reason')}")
                    return self._json(200, {"ok": True})

            self._json(404, {"error": "unknown endpoint", "path": self.path})


def render_page():
    # Leaderboard: best total per (stage, car, driver)
    best = {}
    for r in RESULTS:
        key = (r.get("stage", "?"), r.get("car", "?"), r.get("driver", "?"))
        if key not in best or r.get("total_ms", 1 << 62) < best[key].get("total_ms", 1 << 62):
            best[key] = r
    rows = sorted(best.values(), key=lambda r: (r.get("stage", ""), r.get("total_ms", 0)))

    lb = "".join(
        f"<tr><td>{r.get('stage','?')}</td><td>{r.get('car','?')}</td><td>{r.get('driver','?')}</td>"
        f"<td class=t>{fmt_ms(r.get('total_ms'))}</td><td class=t>{fmt_ms(r.get('raw_ms'))}</td>"
        f"<td class=t>{int(r.get('penalty_ms',0))//1000}s</td></tr>"
        for r in rows
    ) or "<tr><td colspan=6 class=muted>No results yet — finish a stage.</td></tr>"

    def state_label(s):
        st = s.get("state")
        if st == "aborted" and s.get("reason"):
            return f"aborted: {s['reason']}"
        return st

    live = "".join(
        f"<tr><td>{sid}</td><td>{s.get('driver','?')}</td><td>{s.get('car','?')}</td>"
        f"<td>{state_label(s)}</td><td class=t>{s.get('heartbeats',0)}</td>"
        f"<td class=t>{fmt_ms((s.get('last_hb') or {}).get('current_ms'))}</td></tr>"
        for sid, s in reversed(list(SESSIONS.items()))
    ) or "<tr><td colspan=6 class=muted>No sessions yet.</td></tr>"

    return f"""<!doctype html><html><head><meta charset=utf-8>
<meta http-equiv=refresh content=2>
<title>acrally dev server</title>
<style>
 body{{font:14px system-ui,sans-serif;margin:24px;background:#0f1115;color:#e6e6e6}}
 h1{{font-size:18px}} h2{{font-size:14px;color:#8ab4ff;margin-top:28px}}
 table{{border-collapse:collapse;width:100%;margin-top:6px}}
 th,td{{text-align:left;padding:6px 10px;border-bottom:1px solid #262a33}}
 th{{color:#9aa4b2;font-weight:600}} .t{{font-variant-numeric:tabular-nums;text-align:right}}
 .muted{{color:#666}} tr:hover td{{background:#161a22}}
</style></head><body>
<h1>🏁 acrally dev server <span class=muted>· port {PORT} · auto-refresh 2s</span></h1>
<h2>LEADERBOARD (best penalised total per stage / car / driver)</h2>
<table><tr><th>Stage</th><th>Car</th><th>Driver</th><th class=t>Total</th><th class=t>Raw</th><th class=t>Penalty</th></tr>{lb}</table>
<h2>SESSIONS (live + past)</h2>
<table><tr><th>Session</th><th>Driver</th><th>Car</th><th>State</th><th class=t>HBs</th><th class=t>Last cur</th></tr>{live}</table>
</body></html>"""


if __name__ == "__main__":
    try:
        sys.stdout.reconfigure(line_buffering=True)  # live logs even when piped
    except Exception:
        pass
    load()
    print(f"acrally dev server on http://127.0.0.1:{PORT}  (leaderboard in your browser)")
    print(f"point the agent at:  api_base = \"http://127.0.0.1:{PORT}\"")
    ThreadingHTTPServer(("0.0.0.0", PORT), Handler).serve_forever()
