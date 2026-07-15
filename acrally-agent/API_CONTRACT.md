# acrally-agent ↔ backend API contract

This document specifies the HTTP contract between the **acrally-agent** (the Rust
telemetry agent, see `src/submit.rs`, `src/model.rs`, `src/runner.rs`) and the
backend it reports to. The bundled `dev-server.py` is a reference implementation
of this contract; any server implementing the four endpoints below can replace it.

## Transport & authentication

- All agent→server calls are `POST` with a JSON body (`Content-Type: application/json`).
- Base URL defaults to `https://fourleft.io/acrally-api`, overridable via
  `api_base` in `config.toml` or the `ACRALLY_API_BASE` env var (e.g.
  `http://127.0.0.1:8799` for the dev server). Trailing slashes are stripped;
  endpoint paths below are appended directly.
- If `api_key` is set in the config, every request carries
  `Authorization: Bearer <api_key>`. Otherwise no auth header is sent.
- Request timeout on the agent side is **10 seconds**.
- **Any 2xx response counts as success.** Non-2xx status codes and network
  errors are treated as failures (with per-endpoint consequences described below).

## Session lifecycle

```
driving starts        POST /sessions                 → { "session_id": "..." }
while driving         POST /sessions/{id}/heartbeat   (about once a second)
stage finishes        POST /sessions/{id}/result      (authoritative, penalised time)
   — or —
run abandoned         POST /sessions/{id}/abort       { "reason": "..." }
```

A session is opened when the agent detects the car moving on a stage.

**Results are decoupled from the session lifecycle.** The agent watches the
game's save file continuously and posts *every* record that appears while it is
running, plus — on launch — records that landed while it was closed (everything
newer than the last successfully delivered record, whose identity is persisted
locally). Novelty is judged on `timestamp_ticks` **and** the times together:
the tick is stamped when the player *enters* an event and the game overwrites
that event's single record slot on each completed run, so a second run of the
same event arrives with the same tick and different times. Each result is
attached to the most plausible session: the oldest finished session still
waiting for its record, else the currently live session, else the most recently
opened one, else a fresh recovery session opened just for it (empty `driver`).
Live-session detection is heuristic (a glitchy shared-memory timer), so this
attachment is best-effort — the result payload itself is always authoritative.

Sessions still end with an abort when no result is expected. Reasons the agent
sends:

- `"restart"` — the driver restarted the stage mid-run,
- `"superseded"` — a new run started while the previous session was still open,
- `"no-result"` — the stage finished but no save record appeared within the
  save-wait window (3 minutes). This is bookkeeping, not a verdict: the watcher
  keeps running, and a record that lands later is still posted — possibly to
  this same session id.

Consequently a server must accept a result for a session it already saw
complete or abort (storing it and de-duping by `(timestamp_ticks, total_ms)`);
see *Delivery guarantees* below.

---

## Endpoints

### 1. `POST /sessions` — open a session

Called once when driving starts.

Request body (`SessionStart`):

| Field           | Type          | Required | Notes                                            |
|-----------------|---------------|----------|--------------------------------------------------|
| `driver`        | string        | yes      | Driver name as reported by the game              |
| `car`           | string        | yes      | Car identifier                                   |
| `stage`         | string        | yes      | Stage name (currently same value as `track`)     |
| `track`         | string        | yes      | Track name                                       |
| `started_at_ms` | integer (u128)| yes      | Unix epoch milliseconds at session start         |
| `agent_version` | string        | yes      | Agent's Cargo package version, e.g. `"0.1.0"`    |
| `recovery`      | boolean       | no       | `true` when this session replays a save record (startup recovery) instead of tracking a live run. The server must not bind a waiting event arm to a recovery session — recovered runs never score an armed stage. Absent/`false` for live sessions. |

Response — JSON object containing the session id. The agent accepts either key:

```json
{ "session_id": "srv-42" }
```
or
```json
{ "id": "srv-42" }
```

The id is an opaque string; the agent uses it verbatim in the URL path of all
follow-up calls.

**Failure behaviour:** if this call fails (network error, non-2xx, or a body
without a usable id), the agent generates a local id of the form
`local-<nanoseconds>` and continues. Heartbeats and the eventual result will be
keyed by that id. Whether to accept results for ids the server never issued is
a server policy decision — rejecting them is valid and closes a forgery
avenue, at the cost of losing results from runs whose session-open failed.

### 2. `POST /sessions/{id}/heartbeat` — live telemetry

Sent while driving, at most once per second.
**Best-effort:** the response body is ignored and failures are silently
dropped — heartbeats are never retried.

Request body (`Heartbeat`):

| Field        | Type    | Required | Notes                                                  |
|--------------|---------|----------|--------------------------------------------------------|
| `current_ms` | integer | no       | In-progress stage time in ms; omitted when the timer isn't running |
| `speed_kmh`  | number  | yes      | Live speed, km/h                                       |
| `gear`       | integer | yes      | Current gear                                           |
| `rpm`        | integer | yes      | Engine RPM                                             |
| `distance_m` | number  | yes      | Distance travelled on the stage, metres                |

Response: any 2xx; body ignored (dev server returns `{ "ok": true }`).

### 3. `POST /sessions/{id}/result` — authoritative result

Sent when a new record is read from the game's save file. This is the payload
that matters for leaderboards. The `{id}` is the agent's best attribution (see
*Session lifecycle*); it may belong to a session that already received a result
or an abort, and the same session id can receive more than one result when
run-detection missed a start — de-dupe on `(timestamp_ticks, total_ms)`, not
on session state.

Request body (`ResultPayload`):

| Field             | Type          | Required | Notes                                                       |
|-------------------|---------------|----------|-------------------------------------------------------------|
| `stage`           | string        | yes      | Stage name                                                  |
| `car`             | string        | yes      | Car identifier                                              |
| `driver`          | string        | yes      | Driver name                                                 |
| `raw_ms`          | integer (u32) | yes      | Raw stage time, ms                                          |
| `penalty_ms`      | integer (u32) | yes      | Penalty time, ms                                            |
| `total_ms`        | integer (u32) | yes      | Penalised total (`raw_ms + penalty_ms`), ms                 |
| `timestamp_ticks` | integer (i64) | yes      | Save-file event-entry timestamp in .NET ticks — de-dupe key **together with `total_ms`** (runs of one event share the tick) |
| `agent_version`   | string        | yes      | Agent version                                               |

Response: any 2xx; body ignored (dev server returns `{ "ok": true }`).

**Failure behaviour:** a failed result POST is retried up to 3 times
in-process (2 s apart), then dropped with an error log. Results are
deliberately **never persisted to disk** — an editable spool file would let a
user modify or replay results before delivery.

### 4. `POST /sessions/{id}/abort` — abandon a session

Best-effort, like heartbeats: response ignored, no retry.

Request body:

```json
{ "reason": "restart" }
```

`reason` is one of `"restart"`, `"superseded"`, `"no-result"` (see lifecycle
above). Response: any 2xx.

---

## Delivery guarantees & server requirements

1. **Results are best-effort with bounded retries.** A result is sent up to 3
   times in a row while the agent is running, then dropped. There is no
   on-disk spool of result payloads. The agent does persist the *identity*
   (tick + times) of the last delivered record, and on launch re-reads the
   game's save and posts anything newer — so a result missed while the agent
   was closed, or whose delivery failed, is usually recovered from the save
   itself on the next start (the save keeps only the ~10 most recent event
   entries, one slot each, so this recovery is bounded and can still miss
   overwritten runs). Retries and recovery mean the same result can arrive
   more than once, so **de-duplicate by `(timestamp_ticks, total_ms)`** —
   the tick alone is shared by every run of one event entry.
2. **Unknown session ids may appear.** When `/sessions` failed, the result of
   that run arrives under an agent-generated `local-<ns>` id. Whether to
   accept those is server policy: rejecting closes a forgery avenue, accepting
   maximizes result capture. Both are contract-compliant.
3. **Heartbeats and aborts are at-most-once.** They may simply never arrive
   (network hiccup, agent crash). A server should treat a session with no
   result and no abort as stale after a timeout rather than waiting forever.
4. **Response bodies are ignored** except for `POST /sessions`, where the agent
   extracts `session_id` (or `id`). Everything else only needs a 2xx status.
5. **No GET endpoints are part of the contract.** The dev server's `GET /`
   (HTML leaderboard) and `GET /state` (JSON dump of sessions + results) are
   dev-only conveniences, not something the agent uses.

## Example exchange

```
POST /sessions
{"driver":"Busata","car":"Lancia Delta HF Integrale","stage":"Col de Turini",
 "track":"Col de Turini","started_at_ms":1751790000000,"agent_version":"0.1.0"}
← 200 {"session_id":"srv-7"}

POST /sessions/srv-7/heartbeat            (repeats ~1/s)
{"current_ms":83456,"speed_kmh":142.3,"gear":4,"rpm":6800,"distance_m":2450.0}
← 200 {"ok":true}

POST /sessions/srv-7/result
{"stage":"Col de Turini","car":"Lancia Delta HF Integrale","driver":"Busata",
 "raw_ms":290937,"penalty_ms":20000,"total_ms":310937,
 "timestamp_ticks":638567123456789012,"agent_version":"0.1.0"}
← 200 {"ok":true}
```

## Relevant agent configuration

| Config key       | Default                          | Effect on the contract                     |
|------------------|----------------------------------|--------------------------------------------|
| `api_base`       | `https://fourleft.io/acrally-api`| Base URL all endpoint paths are appended to |
| `api_key`        | unset                            | Enables `Authorization: Bearer` header      |

The heartbeat interval (1s), the save-poll cadence (0.5s while driving or
awaiting a record, 5s idle) and the post-finish save-wait (3 minutes, after
which the session aborts with `"no-result"` while the watcher keeps running)
are fixed internal timings, not configurable.
