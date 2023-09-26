CREATE TABLE alias_update_request
(
    id              UUID NOT NULL,
    discord_id      VARCHAR(255),
    requested_alias VARCHAR(255),
    CONSTRAINT pk_aliasupdaterequest PRIMARY KEY (id)
);

CREATE TABLE player_info_aliases
(
    player_info_id UUID NOT NULL,
    aliases        VARCHAR(255)
);

ALTER TABLE player_info_aliases
    ADD CONSTRAINT fk_playerinfo_aliases_on_player_info FOREIGN KEY (player_info_id) REFERENCES player_info (id);

ALTER TABLE player_info_racenets
    DROP CONSTRAINT fk_playerinfo_racenets_on_player_info;

DROP TABLE player_info_racenets CASCADE;

update player_info set racenet=display_name;


alter table player_info_aliases add unique (aliases);
CREATE TABLE alias_update_log
(
    id         UUID NOT NULL,
    discord_id VARCHAR(255),
    changes    VARCHAR(255),
    CONSTRAINT pk_aliasupdatelog PRIMARY KEY (id)
);

ALTER TABLE alias_update_request
    ADD requested_update_time TIMESTAMP WITHOUT TIME ZONE;