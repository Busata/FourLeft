-- Profile claims (V038) compare discord ids as plain snowflakes ("234748363453628416"), but ~98% of existing
-- rows were written by an older Discord library as "Snowflake{234748363453628416}", so their holders weren't
-- recognised. Normalise both tables; the original values stay in *_legacy columns. Revert SQL in
-- docs/profile-claims.md.
ALTER TABLE profile
    ADD COLUMN discord_id_legacy VARCHAR(255);
UPDATE profile
SET discord_id_legacy = discord_id,
    discord_id        = regexp_replace(discord_id, '^Snowflake\{([0-9]+)\}$', '\1')
WHERE discord_id LIKE 'Snowflake{%';

ALTER TABLE profile_update_request
    ADD COLUMN discord_id_legacy VARCHAR(255);
UPDATE profile_update_request
SET discord_id_legacy = discord_id,
    discord_id        = regexp_replace(discord_id, '^Snowflake\{([0-9]+)\}$', '\1')
WHERE discord_id LIKE 'Snowflake{%';
