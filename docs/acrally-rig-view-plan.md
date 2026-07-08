# ACRally rig view — plan

A mobile-friendly page on the website that mirrors the agent's **Races** tab: arm a
stage, watch the live run, see the outcome — so someone at a sim rig can use their
phone instead of alt-tabbing to the desktop agent.

Status: **parked** (plan only, not started). Written 2026-07-08.

## Why this is cheap

The exploration confirmed the hard parts already exist server-side:

- **Arming is server state, not agent state.** The agent's Races tab is a thin
  client: it polls `GET /agent/races` every 4 s and posts `/agent/races/arm` /
  `disarm`. The arm lives in `event_arm` (one live arm per user, partial unique
  index), is bound server-side to the user's *next* session, and the verdicts
  (`RECORDED`, `SLOWER`, `WRONG_STAGE`, `WRONG_CAR`, `EVENT_CLOSED`, `DNF`) are
  computed on the backend and merely rendered by the agent.
  See `AgentRacesEndpoint.java`, `EventArm.java`, `EventRecordingService.java`.
- **Live run data reaches the backend.** Heartbeats (1/s) overwrite a live
  snapshot on `agent_session`: `current_ms`, `speed_kmh`, `distance_m`,
  `last_heartbeat_at`. Already browser-visible via `GET /me/sessions`
  (`MySessionTo`), just fetched one-shot today (`acrally-stats.ts`).
- **Results are fully available**: `/me/results`, `/events/{eventId}/leaderboard`,
  and the arm outcome + final time in `ArmState`.

The only gap: the races endpoints are **agent-only** (Bearer API key →
`AgentPrincipal`; `ApiKeyAuthFilter` covers `/sessions/**` + `/agent/races/**`).
A browser session cannot call them. Internally everything is keyed by `userId`,
so exposing a session-authed twin is a thin controller, not a redesign.

What stays agent-only (fine — the phone doesn't need it): rich telemetry beyond
the heartbeat snapshot (gear/RPM/sector), drive-state detection, save-file
recovery. Note there is **no tick history** — heartbeats overwrite one snapshot
per session — so the live view shows "now", not a trace. That matches the use case.

## Design decisions

- **Reuse, don't fork.** New session-authed endpoints delegate to the same
  service the agent endpoint uses. Because arm state is shared, the agent UI and
  the phone stay in sync automatically within one poll cycle (agent polls 4 s),
  including the "can't disarm once `BOUND`" rule (server 409s).
- **One combined GET for the phone.** The page polls a single endpoint returning
  events + arm state + live session snapshot, instead of three calls per tick.
- **Polling, no push.** The whole module is poll-based (no websocket/SSE
  anywhere); the frontend already has a canonical polling pattern
  (`work-queue.ts:77-97`: `interval` + `startWith` + `switchMap` + signals).
  ~3 s interval.

## Phase 1 — Backend: session-authed races endpoints

New controller `MeRacesEndpoint` under `/acrally-api/me/races` (session cookie,
principal `AppUserDetails`, covered by the existing "authenticated" catch-all in
`SecurityConfig` — no security config change needed):

1. `GET /me/races` → `RigViewTo`:
   - `events` — same open-events view the agent gets (event label, championship,
     club, closes-at, permitted cars, stage rows with `myBestMs`),
   - `arm` — current `ArmState` incl. `lastOutcome` / `lastStageLabel` /
     `lastTotalMs`,
   - `liveSession` — the user's most recent `OPEN` session snapshot
     (`currentMs`, `speedKmh`, `distanceM`, `lastHeartbeatAt`, car, stage) or null.
2. `POST /me/races/arm` body `{eventId, variantId}` → arm state.
   `POST /me/races/disarm` → arm state (409 when `BOUND`, same as agent path).
3. Extract/reuse: the logic behind `AgentRacesEndpoint` (races view assembly,
   arm/disarm rules) moves to / already lives in a service keyed by `userId`;
   both controllers become thin adapters. Camel-case browser DTOs in
   `api.acrally` (the agent DTOs are snake_case wire types — don't mix), so the
   typescript-generator emits them to `server-models.d.ts`.
4. CSRF: browser POSTs go through the normal `XSRF-TOKEN` flow (Angular's built-in
   interceptor) — nothing to add, just don't put `/me/races` in the CSRF-ignore list.

## Phase 2 — Frontend: `acrally/rig` page

Mobile-first standalone component `pages/acrally-rig/`, route behind `authGuard`
(top-level `acrally/rig` route — usable full-screen on a phone without the
dashboard tab chrome; add a link from the dashboard and/or shell nav).

1. **Event cards → stage rows**: stage label, personal best, **Start** button
   with a confirm step (mirrors the agent's `event_card`/`stage_row`/
   `confirm_modal` in `acrally-agent/src/ui.rs`). One arm at a time — Start
   disabled while armed or a call is in flight.
2. **Armed banner**: stage label, permitted cars, Disarm button (disabled once
   `BOUND`).
3. **Live strip** while a session is open: running stage time extrapolated
   client-side from `currentMs` + `lastHeartbeatAt` so it ticks smoothly between
   polls; speed; a "signal" freshness indicator (green &lt; 5 s since heartbeat,
   grey/stale otherwise).
4. **Outcome banner** after the run: `RECORDED` with formatted total, `SLOWER`,
   `WRONG_STAGE`, `WRONG_CAR`, `EVENT_CLOSED`, `DNF`; link into the event
   leaderboard page.
5. Poll `GET /me/races` every ~3 s via the work-queue pattern
   (`takeUntilDestroyed`); pause polling when `document.hidden`.
6. Styling per house conventions: mobile-base styles, enhancements at
   720/880 px, CSS vars from `styles.scss`. Big touch targets — this is used
   mid-session with a wheel in front of you.

## Phase 3 — Rig polish (optional)

- **Screen wake lock** (`navigator.wakeLock`) while armed or a session is live,
  released on outcome/disarm; re-acquire on `visibilitychange`.
- Optional inline mini-leaderboard for the armed stage (top 5 + your rank) from
  `GET /events/{eventId}/leaderboard`, fetched on outcome rather than polled.
- Maybe: surface the arm-warning hints the agent shows (wrong car / wrong stage
  heuristics) — needs those heuristics server-side; agent computes them locally
  today, so skip unless demand shows up.

## Explicitly out of scope

- No websocket/SSE infrastructure — polling matches the module's architecture.
- No per-tick telemetry storage / delta traces (heartbeats stay one overwritten
  snapshot per session).
- No changes to the agent or the ingest contract (`API_CONTRACT.md` untouched);
  the agent keeps working as-is and simply reflects arm changes made from the
  phone on its next 4 s poll.
