# backend-acrally — AC Rally module

Self-hosted module for **Assetto Corsa Rally**. We host clubs/championships ourselves and
let the `acrally-agent` (Rust telemetry agent, see `API_CONTRACT.md`) submit stage results.
Identity is anchored to Steam so people can't masquerade as others or evade bans by
re-registering.

Mirrors the `backend-ea-sports-wrc` module conventions but is **fully independent**: its own
Spring Boot app, its own Postgres DB, its own auth. It does **not** share a database or broker
with the WRC backend. Full design rationale lives in `docs/acrally-module-plan.md` (phase log).

## Where things live

- **`backend-acrally/`** — this module. Spring Boot app on port **8085**, package
  `io.busata.fourleft.backendacrally`. Route prefix for everything is `/acrally-api/`.
- **`api/` module** — shared DTOs under `io.busata.fourleft.api.acrally.*`. The
  `typescript-generator` plugin emits these to
  `frontend/src/app/common/generated/server-models.d.ts`. **Add browser-facing DTOs here**,
  not in this module, or the frontend can't see them. Rebuild `api` to regenerate the `.d.ts`.
- **`frontend/`** — Angular pages under `acrally/…` routes (`frontend/src/app/pages/acrally-*`).
- **`devops/reverse-proxy/fourleft.conf`** — prod routing: `/acrally-api/` → `acrally` upstream
  (`spring.fourleft.backend-acrally:8085`). Also serves the agent release channel under
  `/acrally-agent/` (static `latest.json` + versioned binaries).

## Internal layout (this module)

- `endpoints/` — `@RestController`s (thin; delegate to services).
- `domain/models/<aggregate>/` — JPA entities grouped by aggregate (user, identity, club,
  championship, car, stage, session, agent).
- `domain/services/<aggregate>/` — repositories + services per aggregate.
- `infrastructure/security/` — `SecurityConfig`, `ApiKeyAuthFilter`, `CsrfCookieFilter`,
  `AppUserDetails*`, `AgentPrincipal`, `Tokens`.
- `infrastructure/steam/` — `SteamOpenIdClient` (OpenID 2.0 verify), `SteamWebApiClient` (profile).
- `application/ingest/` — `SessionIngestService` + `IngestPayloads` (agent wire DTOs).
- `resources/db/migration/` — Flyway. `V0xx__*.sql` versioned; `R__seed_*.sql` repeatable seeds.

## Auth model (three principals — do not conflate)

1. **Browser = cookie session** (not JWT). `HttpOnly` session cookie, same-origin behind the
   proxy. CSRF via `XSRF-TOKEN` cookie ↔ `X-XSRF-TOKEN` header (Angular's standard setup,
   plain non-XOR handler). Instantly revocable server-side — critical for banning abusers.
2. **Agent = personal API key** — opaque `acr_`-prefixed bearer token, stored **SHA-256 hashed**,
   shown once. Sent as `Authorization: Bearer <key>` on `/sessions/**`. Handled by
   `ApiKeyAuthFilter` → `AgentPrincipal` (rejects revoked key / banned user). CSRF-exempt.
   Agents obtain keys via **device pairing** (RFC 8628 device-authorization grant), not copy-paste.
3. **Admin** — a session with `ROLE_ADMIN`. Guards everything under `/acrally-api/admin/**`.

Route auth is declared in `infrastructure/security/SecurityConfig.java`:
- Public: `/health`, `POST /auth/register`, `POST /auth/login`, `POST /agent/pair/{start,token}`.
- Admin-only: `/admin/**`.
- Everything else: authenticated session.
- Watch out: the `.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()` line is load-bearing
  — without it a bearer-authed 404 gets re-authorized on the ERROR dispatch and masked as 401.

**Anti-abuse gate:** a linked Steam identity is required before approving an agent pairing or
submitting results. `linked_identity` has unique `(provider, provider_user_id)` and
`(provider, user_id)` — that's what enforces "one Steam = one account."

## Endpoint surface (all under `/acrally-api`)

- `auth/` — register, login, logout, me; `auth/steam/{start,return}` (OpenID 2.0 link flow).
- `account/` — identities, steam profile, API keys (`keys`, `keys/{id}/revoke`).
- `agent/pair/` — `start`, `token` (agent-side); `lookup`, `approve`, `deny` (browser-side).
- `sessions/` — agent ingestion: create, `{id}/heartbeat`, `{id}/result`, `{id}/abort`.
  Server issues UUID ids; `local-*`/unknown/other-user ids → 404. De-dupe on
  `(user_id, timestamp_ticks)`.
- `me/` — `sessions`, `results` (personal dashboard, scoped to `user_id`).
- `clubs/` — list, `mine`, create, `{id}/join`, `{id}/leave`.
- championships — `clubs/{clubId}/championships`, `championships/{id}` (+ `events`, `events/order`),
  `events/{eventId}` (+ `variants`, `cars`).
- `cars`, `variants` — read-only catalogue (`CatalogueEndpoint`).
- `admin/` — `users`, `cars`, `locations`, `stages`, `variants` (CRUD; ROLE_ADMIN).

Agent wire DTOs (`IngestPayloads`) map snake_case with `@JsonProperty`; browser DTOs stay camelCase.

## Local dev

Run with the `override` profile: `-Dspring-boot.run.profiles=override`.

Dedicated local Postgres (own DB, **not** shared with WRC's fl-pg):
```
docker run -d --name fl-pg-acrally -p 17002:5432 \
  -e POSTGRES_USER=backendacrally -e POSTGRES_PASSWORD=backendacrally \
  -e POSTGRES_DB=backendacrally postgres:16
```
Flyway owns the schema; Hibernate is `ddl-auto: validate` (never mutates). `open-in-view: false`.

The override profile points `public-base-url` at `http://localhost:4200` (the `ng serve` origin),
because Steam's `return_to` and the post-link redirect must land on the dev server, which proxies
`/acrally-api` → `:8085` (`frontend/proxy.conf.json`) and serves the `acrally/…` routes. Run the
frontend with `ng serve --proxy-config proxy.conf.json`.

Steam Web API key: prod supplies it via the config server; blank = profile fetch skipped
(linking still works). A local dev key is set in `application-override.yml`.
