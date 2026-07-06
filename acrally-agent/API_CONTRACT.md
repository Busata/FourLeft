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

A session is opened when the agent detects the car moving on a stage. It ends
in exactly one of two ways:

- **result** — the stage was completed and the penalised time was read from the
  game's save file, or
- **abort** — the run ended without a usable result. Reasons the agent sends:
  - `"restart"` — the driver restarted the stage mid-run,
  - `"superseded"` — a new run started while the previous session was still open,
  - `"no-result"` — the stage finished but the save file never produced a
    result within the save-wait window (20 s).

A session receives **at most one** of result/abort from the live flow, but see
*Delivery guarantees* below for result redelivery.

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

Sent once when the stage completes and the result has been read from the game's
save file. This is the payload that matters for leaderboards.

Request body (`ResultPayload`):

| Field             | Type          | Required | Notes                                                       |
|-------------------|---------------|----------|-------------------------------------------------------------|
| `stage`           | string        | yes      | Stage name                                                  |
| `car`             | string        | yes      | Car identifier                                              |
| `driver`          | string        | yes      | Driver name                                                 |
| `raw_ms`          | integer (u32) | yes      | Raw stage time, ms                                          |
| `penalty_ms`      | integer (u32) | yes      | Penalty time, ms                                            |
| `total_ms`        | integer (u32) | yes      | Penalised total (`raw_ms + penalty_ms`), ms                 |
| `timestamp_ticks` | integer (i64) | yes      | Save-file timestamp in .NET ticks — **stable de-dupe key**  |
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
   on-disk spool and no replay after an agent restart: what the server didn't
   receive during the run is gone. Retries mean the same result can still
   arrive more than once, so **de-duplicate by `timestamp_ticks`** (the dev
   server does exactly this).
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

The heartbeat interval (1s) and the post-finish save-wait (20s, after which the
session aborts with `"no-result"`) are fixed internal timings, not configurable.
