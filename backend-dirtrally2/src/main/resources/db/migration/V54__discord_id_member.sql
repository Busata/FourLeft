delete from discord_guild_member;

ALTER TABLE discord_guild_member
    ADD discord_id VARCHAR(255);

ALTER TABLE user_discord_guild_access
    ALTER COLUMN discord_id TYPE VARCHAR(255) USING (discord_id::VARCHAR(255));

ALTER TABLE discord_guild_member
    ALTER COLUMN guild_id TYPE VARCHAR(255) USING (guild_id::VARCHAR(255));

ALTER TABLE discord_guild_member
    DROP COLUMN id;

ALTER TABLE discord_guild_member
    ADD id UUID NOT NULL PRIMARY KEY;

ALTER TABLE user_discord_guild_access_guild_ids
    ALTER COLUMN user_discord_guild_access_discord_id TYPE VARCHAR(255) USING (user_discord_guild_access_discord_id::VARCHAR(255));
