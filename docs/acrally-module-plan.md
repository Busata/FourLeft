# ACRally module — implementation plan

A new self-hosted module for **Assetto Corsa Rally**. We host clubs ourselves and let
the `acrally-agent` (the Rust telemetry agent, see `API_CONTRACT.md`) submit results.
Before results, we build the account loop: users register with email/password and
anchor their identity with Steam, so people can't masquerade as someone else or evade
bans by spinning up new accounts.

This mirrors the existing `backend-ea-sports-wrc` module conventions:

- new **Spring Boot module** `backend-acrally` (port **8085**, its own Postgres DB),
- shared DTOs in the **`api`** module under `io.busata.fourleft.api.acrally.*`
  (the `typescript-generator` plugin emits them to
  `frontend/src/app/common/generated/server-models.d.ts`),
- **Angular** pages under `acrally/…` in the existing `frontend`,
- a new **reverse-proxy** route: `/acrally-api/` → an `acrally` upstream
  (the existing `/api_v2/` prefix is hardwired to `easportswrc`).

## Key decisions (locked)

- **Browser auth = cookie session**, not JWT. The SPA is same-origin behind the
  reverse proxy, so an `HttpOnly` session cookie is simpler and safer than a JWT in
  `localStorage` (XSS can't read it), needs no refresh-token machinery, and — crucially
  for a system whose job is banning abusers — is **instantly revocable** (delete the
  session server-side). Stateless JWTs stay valid until expiry.
- **Agent auth = revocable personal API key** (opaque bearer token, stored hashed).
  The contract already sends `Authorization: Bearer <api_key>`. Long-lived + revocable
  is exactly right for a headless agent, and shares the "killable" property with sessions.
  Not a JWT.
- **Identity providers are generalized** into a `linked_identity` table
  (`provider`, `provider_user_id`, `user_id`) rather than a Steam-specific table. Steam
  is the first provider; Discord/Epic/etc. slot in later with no schema change. Steam is
  linked **after** registration (email/password is the credential), and the door stays
  open to later flip a provider into a login method.
- **Steam login is OpenID 2.0**, not OAuth2/OIDC — so `spring-security-oauth2-client`
  does not apply. It's a small custom flow (redirect to Steam, verify the returned
  `claimed_id` via a `check_authentication` POST). No heavy dependency.

---

## Phase 0 — Scaffold the module (no behavior yet)

An empty-but-running `backend-acrally` wired into the build and infra, proving the
deploy path before any auth logic. Kept dependency-light (no DB yet) so it boots with
zero external services.

1. New module `backend-acrally/` (Boot parent, deps: `api`, `web`, `spring-cloud-starter-config`,
   `lombok`, test). Add to root `pom.xml` `<modules>`.
2. Package `io.busata.fourleft.backendacrally`, main app class,
   `application.yml` (port 8085, `spring.application.name: backend-acrally`,
   optional configserver import) + `application-override.yml` for local dev.
3. New `api.acrally` package in the `api` module for DTOs (keeps TS generation working).
4. Trivial `GET /acrally-api/health` endpoint to verify the full chain.
5. Infra: `db.backend-acrally` (Postgres) + `spring.fourleft.backend-acrally` in
   `docker-compose.yml`, `backend-acrally/Dockerfile`, and `upstream acrally` +
   `location /acrally-api/` in `devops/reverse-proxy/fourleft.conf`.

## Phase 1 — Email/password auth foundation

Register, log in, log out; an authenticated browser session.

1. Add `spring-boot-starter-security` + `spring-boot-starter-data-jpa` + `flyway` +
   `postgresql` to the module (scoped to `backend-acrally` only).
2. `V001__users.sql`: `app_user` (id, email unique-ci, password_hash, display_name,
   status, created_at).
3. `SecurityConfig` (`SecurityFilterChain`), `BCryptPasswordEncoder`, cookie session,
   CSRF token endpoint for the SPA.
4. Endpoints: `POST /acrally-api/auth/register`, `/auth/login`, `/auth/logout`,
   `GET /acrally-api/auth/me`.

## Phase 2 — Steam identity anchor (anti-abuse)

Bind one Steam account ↔ one app account.

1. `V002__linked_identity.sql`: `linked_identity` (provider, provider_user_id,
   user_id, linked_at) with a unique `(provider, provider_user_id)` and a unique
   `(provider, user_id)` — this is what enforces "one Steam = one account."
2. Custom Steam OpenID 2.0 flow: `GET /auth/steam/start` (redirect),
   `GET /auth/steam/return` (verify + link to the logged-in user).
3. Policy gate: a linked Steam is required before minting an agent key or submitting
   results. Bans live on the account; Steam uniqueness blocks re-register.

## Phase 3 — Frontend auth UX

1. Angular pages under `acrally/`: `register`, `login`, `account` (linked Steam +
   "Link Steam").
2. Auth guard + auth signal/service; `withInterceptors` for credentials/CSRF;
   `acrally/…` routes in `app.routes.ts`; nav entry in the shell; `/acrally-api` added
   to `frontend/proxy.conf.json` for local dev.
3. Regenerate `server-models.d.ts` via the `api` module build.

## Phase 4a — Low-friction agent login (device pairing) — DONE

Instead of copy-pasting a key, the agent uses an **OAuth device-authorization grant**
(RFC 8628): "enter this code / click this link", like logging a TV into an account.

1. `api_key` (hashed, shown once) + `device_pairing` tables (V004). `Tokens` util:
   `acr_`-prefixed keys, unambiguous user codes, SHA-256 hashing.
2. Agent: `POST /agent/pair/start` → `deviceCode` + `userCode` + `verificationUri(Complete)`;
   polls `POST /agent/pair/token` → `pending` / `approved` (+ `api_key` once) / `denied` /
   `expired` / `consumed`.
3. Browser (session): `GET /agent/pair/lookup`, `POST /agent/pair/approve`
   (**requires a linked Steam** — the anti-abuse gate), `/deny`.
4. `GET /account/keys` + `POST /account/keys/{id}/revoke`.
5. Frontend `acrally/link` page (reads `?code`, bounces through login preserving the code);
   API-keys list on the account page.

## Phase 4b — Result ingestion (the API contract) — DONE

The four `API_CONTRACT.md` endpoints, authenticated by the personal key.

1. `agent_session` + `stage_result` (V005); **de-dupe** unique `(user_id, timestamp_ticks)`.
2. `ApiKeyAuthFilter`: `Authorization: Bearer <key>` on `/acrally-api/sessions/**` →
   `AgentPrincipal` (rejects revoked key / banned user).
3. `POST /sessions` (**server-issued UUID** ids; `local-*`/unknown/other-user → 404),
   `/sessions/{id}/heartbeat`, `/result` (dedupe), `/abort`. Agent wire DTOs
   (`IngestPayloads`) map snake_case with `@JsonProperty` so browser DTOs stay camelCase.
4. Note: fixed a Spring Security 6 quirk where a bearer 404 was masked as 401 on the
   ERROR dispatch — `.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()`.

## Phase 5 — Personal dashboard — DONE

1. `acrally/dashboard`: the user's own results (formatted times/penalties) + recent
   sessions, scoped to `user_id`.
2. `GET /acrally-api/me/sessions`, `/me/results`.
3. (Later, toward club hosting) club leaderboards — beyond this basic loop.

## Remaining

- **Agent-side (Rust):** drive the pairing handshake and persist the returned `api_key` to
  `config.toml`; the backend endpoints are ready.
- **Stale-session sweep:** OPEN sessions with no result/abort → STALE after a timeout
  (scheduled job) — not yet built.
- Club hosting / leaderboards.

---

**Sequencing:** Phases 0→3 deliver the "register + authenticate with Steam" loop;
2.5 enriches the Steam profile; 4a→4b add low-friction agent login + ingestion; 5 the
dashboard. Each phase is independently shippable.
