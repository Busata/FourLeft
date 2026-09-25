# Multi-club channels

A Discord channel can track several clubs, for multi-class championships (the same calendar in several
clubs, one per car class) and multi-tier ones ("JRC 1", "JRC 2", ...).

## Status

**Step 1 — configuration & data model (V033): done.** Nothing user-visible changes.

- `discord_club_configuration_club (configuration_id, position, club_id, label)` holds a channel's clubs.
  Position 0 is the **primary** club.
- `discord_club_configuration.mode` — `SINGLE` (default), `MIXED`, `TIERED`. Only stored for now.
- `channel_id` was already unique (V005): a channel has exactly one configuration, the clubs hang off it.
- Club-driven fan-out (`findByClubId`: autoposting, event ended, championship started, leaderboard
  relay, custom overview restrictions) matches **any** of a channel's clubs, and uses the club from the
  triggering event (event-ended standings included).
- Channel-driven views (`/api_v2/results/{channelId}/*`, `/api_v2/events/{channelId}/summary`,
  restriction targets/vehicles in the config UI) read the **primary** club — identical to before.
- Settings (scoring, restrictions, templates, tracking, TT top) stay channel-wide.

Managing clubs (request-link scoped, like the rest of the config UI):

| | |
|---|---|
| `POST /api_v2/configuration/channel/{requestId}/clubs` `{clubId, label}` | add, or relabel if tracked |
| `DELETE /api_v2/configuration/channel/{requestId}/clubs/{clubId}` | remove; never the last club |
| `PUT /api_v2/configuration/channel/{requestId}/mode` `{mode}` | set `SINGLE`/`MIXED`/`TIERED` |

The legacy `POST /api_v2/configuration/channels` now adds the club to an existing channel configuration
instead of failing on the unique `channel_id` constraint.

**Step 2 — MIXED configuration & compatibility: done.**

- `ChannelClubCompatibilityService` compares every club's current championship (active → upcoming →
  most recent) with the primary's: same event count and, per event (by open date), same location,
  season, stages (route + weather/surface, order-insensitive) and open/close within 1h. Only the car
  class may differ. Checked against local data: the real WRC/WRC2 pair (clubs 1689 + 3597) matches to
  the second; every other same-location overlap in the DB is days apart.
- Effective mode = MIXED only when configured MIXED, ≥2 clubs and compatible; otherwise SINGLE.
- `findPostingForClub` gates club-driven fan-out (autopost, event ended, championship started, channel
  refresh relay): a secondary club only posts while the channel is effectively MIXED.
- `ChannelConfigurationTo` exposes `effectiveMode` + `compatibility` (problems, per-club championship /
  event / class); `ChannelConfigurationUpdateTo.mode` saves the mode with the rest of the form.
- Frontend channel-config: Single / Mixed select, club list (add, relabel, remove), green/red status with
  the mismatch reasons and the "falls back to the primary club" notice.

**Step 3 — merged posting for MIXED: done.** A MIXED channel gets one set of posts for all its clubs.

- `ChannelResultsService` is what every channel post / endpoint reads: the primary club for SINGLE, all
  clubs merged for MIXED. The other clubs' events are *matched* to the primary's (same location, close
  within 1h) — not each club's own current event — so a lagging sync can't mix two events.
- Results: one classification by accumulated time (`MergedRanking` — exactly racenet's per-board order,
  checked against 300 real boards: rank never disagrees with time, DNFs included), gaps to the overall
  leader. Each entry gets `${class}` (club label, else its car class, max 20 chars); templates without it
  get ` • *${class}*` appended. `${classRank}` = the club's own rank. Header: "WRC2 / Rally4", one board
  link per class, no TT link. Event restrictions only apply to the primary club's entries.
- Standings: one post, a titled section per class (racenet keeps one points table per club); the 50-entry
  cap is shared.
- Stats: all clubs' entries together; top 10 is the overall one.
- Autoposts: one stream across clubs; posted entries are keyed event + player (a driver can enter both
  classes); a per-channel lock serialises the clubs' parallel syncs.
- Event ended / championship started: posted once, by the club completing the set — arrivals are
  recorded in `discord_channel_post_gate` (V034) keyed on the primary's matching event/championship.
  If a club never reports (e.g. its sync is broken), the merged post doesn't go out.
- Time trial top: never posted for MIXED channels (`/timetrial` returns empty, toggles hidden).
- Limits: entry lists go through `EmbedBudget` (1024/field, 6000/embed, 25 fields, "…and N more");
  car statistics are capped to one field; autoposts drop their lowest ranks until under 2000 chars (the
  dropped ones post on the next sync).

**Next (not built):** rendering for `TIERED`.

## Rollback

V033 is additive. `discord_club_configuration.club_id` is kept and mirrored from the primary club on
every add/remove, so the previous build runs against the migrated schema unchanged. Only clubs beyond the
primary are invisible to it.

Full revert (after rolling the build back):

```sql
DROP TABLE discord_channel_post_gate;
DELETE FROM flyway_schema_history WHERE version = '034';

-- Sync the legacy column one last time in case it drifted.
UPDATE discord_club_configuration c
SET club_id = cc.club_id
FROM discord_club_configuration_club cc
WHERE cc.configuration_id = c.id AND cc.position = 0;

DROP TABLE discord_club_configuration_club;
ALTER TABLE discord_club_configuration DROP COLUMN mode;
DELETE FROM flyway_schema_history WHERE version = '033';
```
