# Profile claims

Discord ↔ racenet links with a trust state (V038 `profile.claim_state`: CLAIMED / VERIFIED / DISPUTED), claimed
with `/wrc profile` (alias `/wrc track`), disputes confirmed on the profile page, operator page behind V039
`admin_link` (minted into the admin log channel, or with `devops/cli/disputes.sh`).

## V040: discord id normalisation

On 2026-09-29, 395 of 402 `profile.discord_id` values and 883 of 899 `profile_update_request.discord_id` values
were stored as `Snowflake{<id>}` (an older Discord library's `toString`); the rest, and everything written since,
are plain `<id>`. V040 rewrites the wrapped ones to plain and keeps the originals in `discord_id_legacy` (NULL for
rows that were already plain).

Revert:

```sql
UPDATE profile SET discord_id = discord_id_legacy WHERE discord_id_legacy IS NOT NULL;
UPDATE profile_update_request SET discord_id = discord_id_legacy WHERE discord_id_legacy IS NOT NULL;
ALTER TABLE profile DROP COLUMN discord_id_legacy;
ALTER TABLE profile_update_request DROP COLUMN discord_id_legacy;
DELETE FROM flyway_schema_history WHERE version = '040';
```

Profiles claimed (or released) after V040 have no legacy value; reverting only restores the wrapped format on
rows V040 touched.

Revert for V038/V039 is in each migration's header.

## Multi-profile holders

The old `/wrc track` flow let one discord user hold many profiles. On 2026-09-29 one user held 87 — a league
organiser who tags display names with their class (`[PRO]`, `[PRO-AM]`, `[Wildcard]`, `[UC]`) — and a few others
held 2-6. Decision: keep them as holders; their drivers claiming their own account go through a dispute. Longer
term the class tags belong in tier sets.
