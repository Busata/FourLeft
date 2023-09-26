CREATE TABLE user_discord_guild_access
(
    user_id UUID NOT NULL,
    CONSTRAINT pk_userdiscordguildaccess PRIMARY KEY (user_id)
);

CREATE TABLE user_discord_guild_access_guild_ids
(
    user_discord_guild_access_user_id UUID NOT NULL,
    guild_ids                         VARCHAR(255)
);

ALTER TABLE user_discord_guild_access_guild_ids
    ADD CONSTRAINT fk_userdiscordguildaccess_guildids_on_user_discord_guild_access FOREIGN KEY (user_discord_guild_access_user_id) REFERENCES user_discord_guild_access (user_id);

ALTER TABLE discord_guild_access_guild_ids
    DROP CONSTRAINT fk_discordguildaccess_guildids_on_discord_guild_access;

DROP TABLE discord_guild_access CASCADE;

DROP TABLE discord_guild_access_guild_ids CASCADE;