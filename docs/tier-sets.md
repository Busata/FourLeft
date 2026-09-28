# Tier sets

A **tier set** is a named, ordered list of tiers ("JRC 1", "JRC 2", ...) with players assigned to them.
It stands on its own for now; attaching a set to a channel and its clubs (TIERED mode, see
[multi-club-channels.md](multi-club-channels.md)) comes later. "Tier set" is a working name.

## Model (V037)

| Table | |
|---|---|
| `tier_set (id, name, guild_id, created_by, created_at)` | `guild_id` = the Discord server it was created from (NULL from the CLI). Names are unique per guild, case-insensitively (checked by the app). |
| `tier (id, tier_set_id, position, label)` | position 0 = top tier; kept gapless on remove/reorder. |
| `tier_player (tier_id, player_id, display_name)` | `player_id` = racenet ssid (`club_leaderboard_entry.ssid`), `display_name` = name when assigned. A player sits in at most one tier of a set — assigning to another tier moves them. |
| `tier_set_link (id, tier_set_id, discord_id, created_at)` | private edit links; the UUID is the credential (same model as channel configuration requests). |

Everything cascades from `tier_set`.

## Creating and managing

- **Discord** (admin-only `/fourleft`): `/fourleft tiers create name:` creates a set in this server and
  replies with a private edit link; `/fourleft tiers edit name:` (autocompletes the server's sets) mints a
  new link.
- **CLI**: `cli.sh` → *Tier sets* → create (name + optional guild id), or pick an existing set to mint a
  new edit link. Inserts straight into the DB over ssh, like `channel.sh`.
- **Edit page** `https://fourleft.io/easportswrc/tiers/{linkId}`: rename the set; add, rename, reorder
  and remove tiers; add players per tier with autocomplete (every synced club leaderboard entry, matched
  on display name, prefix matches first) and remove them. Every change saves immediately.

## API

| | |
|---|---|
| `POST /api_v2/tier-sets` `{name, guildId, discordId}` | create → `{linkId, name}`; 409 on a duplicate name in the guild |
| `GET /api_v2/tier-sets/guild/{guildId}/names` | names for the bot's autocomplete |
| `POST /api_v2/tier-sets/link-request` `{name, guildId, discordId}` | new edit link; 404 when unknown |
| `GET/PUT /api_v2/tier-sets/link/{linkId}` `{name}` | read / rename |
| `POST /api_v2/tier-sets/link/{linkId}/tiers` `{label}` | add a tier (at the bottom) |
| `PUT/DELETE /api_v2/tier-sets/link/{linkId}/tiers/{tierId}` `{label}` | relabel / remove (players become unassigned) |
| `PUT /api_v2/tier-sets/link/{linkId}/tiers/order` `{tierIds}` | reorder |
| `PUT /api_v2/tier-sets/link/{linkId}/players` `{tierId, playerId, displayName}` | assign / move |
| `DELETE /api_v2/tier-sets/link/{linkId}/players/{playerId}` | unassign |
| `GET /api_v2/tier-sets/link/{linkId}/players/suggest?q=` | autocomplete, ≥2 chars, 10 results |

Link-scoped calls return the whole set. Unknown link → 404; blank or >100-char names → 400.

Like `/api_v2/configuration/channel/request`, the three bot calls are reachable through the public
`/api_v2/` proxy: anyone who knows a guild id and a set name can mint an edit link.

## Revert

Purely additive; to roll back, deploy the previous build and run:

```sql
DROP TABLE tier_set_link;
DROP TABLE tier_player;
DROP TABLE tier;
DROP TABLE tier_set;
DELETE FROM flyway_schema_history WHERE version = '37';
```
