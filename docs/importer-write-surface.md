# Club Importer — Write Surface & Endpoint Design

Analysis of every persistence write the in-process club importer performs, to define the
HTTP endpoints a future **out-of-process importer** would call to push results back.

- **Companion interface:** [`ClubImportApi.java`](../backend-ea-sports-wrc/src/main/java/io/busata/fourleft/backendeasportswrc/endpoints/importapi/ClubImportApi.java)
- **Scope:** the `application.importer.vt.ClubImportWorker` flow and the domain-service methods it calls.

---

## How the importer works today

A club is imported either by the work queue (`application/work/queue/`, when `work-queue.enabled`)
or, when the queue is off, by `ClubUpdateSchedule` (every 5s) → `ClubImporter.sync()`. Both paths
converge on `ClubImportWorker.importClub(clubId)`, which runs on a single virtual thread:

1. It reads current club state via `ClubService` (`exists`, `requires{Detail,Leaderboard,History}Update`,
   …) to decide what this club needs this cycle.
2. It **fetches from Racenet (blocking)** through `BlockingRacenetGateway`, then calls
   **domain services**, which do all the writes inside `@Transactional` methods.

The importer never touches repositories directly. All persistence funnels through
`ClubService`, `ClubLeaderboardService`, `ChampionshipService`, `ClubConfigurationService`
via Spring Data `save`/`saveAll` + one `@Modifying` bulk update. JPA cascades
(`CascadeType.ALL`, `orphanRemoval=true`) propagate into child tables.

**Key consequence:** payloads are the **raw Racenet models**, and all derived logic
(status transitions, dedup, cumulative ranks) is **server-side**. A remote importer can be a
thin fetch-and-push worker; the domain model stays authoritative.

---

## The write operations (the endpoints you need)

| # | Domain call (`file:line`) | Persists | Payload |
|---|---|---|---|
| W1 | `ClubService.createClub` (`ClubService.java:40`) | New **Club** aggregate + cascade Championship/Event/Stage/settings | `ClubDetailsTo` + `List<ChampionshipTo>` |
| W2 | `ClubService.updateClub` (`ClubService.java:135`) | **Club** details, upsert championships/events, recompute statuses, close missing championships, bump `lastDetailsUpdate` | `ClubDetailsTo` + `List<ChampionshipTo>` |
| W3 | `ClubService.markHistoryUpdateDone` (`ClubService.java:71`) | Sets `updatedAfterFinish=true` on finished championships | `clubId` |
| W4 | `ClubService.markBoardAsUpdated` (`ClubService.java:158`, query `ClubRepository.java:13`) | `@Modifying` bulk `UPDATE Event SET lastLeaderboardUpdate` | `leaderboardId` (called inside W7) |
| W5 | `ChampionshipService.save` (`ChampionshipService.java:36`) via `updateStandings` (`ClubService.java:172`) | **Championship** + cascade **ChampionshipStanding** | standings entries |
| W6 | `ClubService.updateStandings` → `clubRepository.save` (`ClubService.java:175`) | **Club** (same tx as W5) | — |
| W7 | `ClubLeaderboardService.updateLeaderboards` (`ClubLeaderboardService.java:55`) | Upsert **ClubLeaderboard** + replace all **ClubLeaderboardEntry** (orphanRemoval) | `LeaderboardUpdatedResult{clubId, leaderboardId, entries}` |
| W8 | `ClubConfigurationService.setClubSync` (`ClubConfigurationService.java:34`) | **ClubConfiguration** `keepSynced=false` | `clubId` |

W4–W6 are internal steps of W7/standings, not separate endpoints.

### Implicit writes (flushed via managed entities)

Handlers load entities with `findById(...)`, mutate them, and rely on tx-commit flush
(most are followed by a redundant explicit `save`):

- **Club** — `updateBasicDetails` (name/description/activeMemberCount), `updateChampionship`, `markUpdated`.
- **Championship** — `updateStatus`, `markClosed` (for championships no longer in `championshipIDs`), `updateEvents` (clears + replaces events, orphan-deletes old rows), `setUpdatedAfterFinish`, `update(standings)`.
- **Event / Stage** — recreated wholesale by `updateEvents`/`updateStages`; status via `updateStatus`/`markClosed`.
- **ChampionshipStanding** — created via `ClubFactory.createStanding`; existing rows mutated (rank, previousRank, pointsAccumulated…).
- **ClubLeaderboard / Entry** — `updateEntries` clears + replaces all entries; `setTotalEntries`.

---

## Input payloads (fields consumed from Racenet)

**Club create/update (W1, W2)** — source `ClubImportWorker.createNewClub/fetchAndUpdateDetails`:

- **Club:** `clubID`, `clubName`, `clubDescription`, `activeMemberCount`, `clubCreatedAt`, `championshipIDs`, `currentChampionship`
- **Championship (`ChampionshipTo`):** `id`, `absoluteOpenDate`, `absoluteCloseDate`, `settings{name, format, bonusPointsMode, scoringSystem, trackDegradation, isHardcoreDamageEnabled, isAssistsAllowed, isTuningAllowed}`, `events[]`
- **Event (`ChampionshipEventTo`):** `id`, `leaderboardID`, `absoluteOpenDate`, `absoluteCloseDate`, `status`, `eventSettings{vehicleClassID, vehicleClass, weatherSeasonID, weatherSeason, locationID, location, duration}`, `stages[]`
- **Stage (`ChampionshipEventStageTo`):** `id`, `leaderboardID`, `stageSettings{routeID, route, weatherAndSurfaceID, weatherAndSurface, timeOfDayID, timeOfDay, serviceAreaID, serviceArea}`

**Leaderboard (W7)** — source `ClubImportWorker.fetchLeaderboard` (paginated). Per entry:
`displayName, nationalityID, platform, rank, vehicle, wrcPlayerId, ssid, time, timePenalty,
timeAccumulated, differenceToFirst`. Derived **server-side**: `rankAccumulated`,
`differenceAccumulated`, dedup by displayName, sort by `timeAccumulated`.

**Standings (W5/W6)** — source `ClubImportWorker.fetchStandings` (paginated). Per entry:
`ssid, displayName, pointsAccumulated, rank, nationalityID`. Deduplicated by `rank` server-side.

---

## Non-write concerns you can't drop

### 1. Domain events must still fire

Published **after** the writes; downstream features depend on them:

| Event | Emitted at | Downstream |
|---|---|---|
| `ClubEventEnded` | `ClubImportWorker.eventEnded`, `ClubImportWorker.updateHistory` | Discord "event ended" message |
| `ClubChampionshipStarted` | `ClubImportWorker.championshipStarted` | Discord "championship started" message |
| `LeaderboardUpdatedEvent` | `ClubImportWorker.updateLeaderboards` | `EventRelayer` → RabbitMQ `ChannelUpdatedEvent` (channel refresh) |

If the importer moves out, **the endpoints must re-publish these** or notifications/relays
break silently. Note `ClubEventEnded` fires once after a *batch* of writes (details + all open
leaderboards + active-championship standings) — hence the explicit `signalEventEnded` in the interface.

### 2. External side-effect

When a club's details fetch fails, `ClubImportWorker` disables sync for that club
(`setClubSync(clubId, false)`) and logs it. (The old state machine also posted an aggregate
Discord "Disabled clubs count: N" message per cycle; that has no cycle boundary here — clubs run
on independent threads — so it was intentionally dropped.)

### 3. The importer is also a reader — the real design fork

What to fetch is decided entirely by DB reads of current club state
(`ClubImportWorker` via `ClubService`): `exists`,
`requires{Detail,Leaderboard,History}Update`, `hasActiveEventThatFinished`,
`hasUpcomingChampionshipThatStarted`, `getOpenLeaderboards`, `getLeaderboardsRequiringUpdate`,
`getHistory{Leaderboards,Championships}`, `getActiveChampionshipId`, `findSyncableClubs`.

- **Option A — thin writer (recommended).** The import flow + decisions stay in this app; the
  remote app only does Racenet fetch + push. Fewest moving parts; DB stays single-owner. The
  write endpoints above are all you need.
- **Option B — autonomous importer.** The remote app owns the decision logic and needs
  **read/GET endpoints** for all the state above (or its own state store). More surface, and you
  must prevent two services writing the same tables.

The write payloads are trivial (raw Racenet models); the importer's real intelligence is choosing
*which* writes to make. Pick who owns that before finalizing the API.

---

## Suggested REST mapping

```
POST /api/import/clubs                                             → W1 createClub
PUT  /api/import/clubs/{clubId}                                    → W2 updateClubDetails   (?reason=DETAILS|CHAMPIONSHIP_STARTED|EVENT_ENDED)
PUT  /api/import/clubs/{clubId}/leaderboards/{leaderboardId}       → W7 (+W4) updateLeaderboard
PUT  /api/import/clubs/{clubId}/championships/{cid}/standings      → W5/W6 updateStandings
POST /api/import/clubs/{clubId}/history/complete                   → W3 markHistoryComplete
PUT  /api/import/clubs/{clubId}/sync                               → W8 setClubSync
POST /api/import/clubs/{clubId}/events/ended                       → signalEventEnded (emit ClubEventEnded after a batch)
```

W1 and W2 can be merged into a single upsert if you prefer.
