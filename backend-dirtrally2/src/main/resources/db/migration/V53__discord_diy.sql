CREATE TABLE discord_guild_member
(
    id        VARCHAR NOT NULL,
    user_name VARCHAR(255),
    guild_id  VARCHAR,
    CONSTRAINT pk_discordguildmember PRIMARY KEY (id)
);

delete from user_discord_guild_access_guild_ids;
delete from user_discord_guild_access;

ALTER TABLE user_discord_guild_access_guild_ids
    DROP CONSTRAINT fk_userdiscordguildaccess_guildids_on_user_discord_guild_access;

ALTER TABLE user_discord_guild_access
    DROP COLUMN user_id;

ALTER TABLE user_discord_guild_access_guild_ids
    DROP COLUMN user_discord_guild_access_user_id;

ALTER TABLE user_discord_guild_access
    ADD discord_id VARCHAR;

ALTER TABLE user_discord_guild_access_guild_ids
    ADD user_discord_guild_access_discord_id VARCHAR;

ALTER TABLE user_discord_guild_access
    ADD CONSTRAINT pk_userdiscordguildaccess PRIMARY KEY (discord_id);

ALTER TABLE user_discord_guild_access_guild_ids
    ALTER COLUMN user_discord_guild_access_discord_id SET NOT NULL;

ALTER TABLE user_discord_guild_access_guild_ids
    ADD CONSTRAINT fk_userdiscordguildaccess_guildids_on_user_discord_guild_access FOREIGN KEY (user_discord_guild_access_discord_id) REFERENCES user_discord_guild_access (discord_id);
