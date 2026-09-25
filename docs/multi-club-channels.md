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

**Next (not built):** rendering for `MIXED` / `TIERED` — results, standings, autoposts (today each club
starts its own autopost message and each club's event-ended post arrives separately), Next-button cycle,
frontend club list editor.

## Rollback

V033 is additive. `discord_club_configuration.club_id` is kept and mirrored from the primary club on
every add/remove, so the previous build runs against the migrated schema unchanged. Only clubs beyond the
primary are invisible to it.

Full revert (after rolling the build back):

```sql
-- Sync the legacy column one last time in case it drifted.
UPDATE discord_club_configuration c
SET club_id = cc.club_id
FROM discord_club_configuration_club cc
WHERE cc.configuration_id = c.id AND cc.position = 0;

DROP TABLE discord_club_configuration_club;
ALTER TABLE discord_club_configuration DROP COLUMN mode;
DELETE FROM flyway_schema_history WHERE version = '033';
```
