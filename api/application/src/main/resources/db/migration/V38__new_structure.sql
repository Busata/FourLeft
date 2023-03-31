CREATE TABLE discord_guild_access
(
    id                    UUID NOT NULL,
    discord_user_id       VARCHAR(255),
    last_invite_sent_time TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_discordguildaccess PRIMARY KEY (id)
);

CREATE TABLE discord_guild_access_guild_ids
(
    discord_guild_access_id UUID NOT NULL,
    guild_ids               VARCHAR(255)
);

CREATE TABLE player_filter_racenet_names
(
    player_filter_id UUID NOT NULL,
    racenet_names    VARCHAR(255)
);

ALTER TABLE discord_channel_autopost_configurations
    ADD CONSTRAINT uc_discordchannelautopostconfigurations_clubviewconfiguration UNIQUE (club_view_configuration_id);

ALTER TABLE discord_channel_club_view_configurations
    ADD CONSTRAINT uc_discordchannelclubviewconfigur_commandsclubviewconfiguration UNIQUE (commands_club_view_configuration_id);

ALTER TABLE discord_guild_access_guild_ids
    ADD CONSTRAINT fk_discordguildaccess_guildids_on_discord_guild_access FOREIGN KEY (discord_guild_access_id) REFERENCES discord_guild_access (id);

ALTER TABLE player_filter_racenet_names
    ADD CONSTRAINT fk_playerfilter_racenetnames_on_player_filter FOREIGN KEY (player_filter_id) REFERENCES player_filter (id);

ALTER TABLE player_filter_player_names
DROP
CONSTRAINT fk_playerfilter_playernames_on_player_filter;

DROP TABLE player_filter_player_names CASCADE;

ALTER TABLE club_view
DROP
COLUMN description;

ALTER TABLE club_view
DROP
COLUMN badge_type;

ALTER TABLE player_filter
DROP
COLUMN name;

ALTER TABLE club_view
    ADD badge_type INTEGER;