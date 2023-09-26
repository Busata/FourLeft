CREATE TABLE player_info_racenets
(
    player_info_id UUID NOT NULL,
    racenets       VARCHAR(255)
);

ALTER TABLE player_info
    ADD discord_id VARCHAR(255);

ALTER TABLE player_info
    ADD display_name VARCHAR(255);

ALTER TABLE board_entry
    ADD player_info_id UUID;

ALTER TABLE player_info
    ADD CONSTRAINT uc_playerinfo_displayname UNIQUE (display_name);

ALTER TABLE board_entry
    ADD CONSTRAINT FK_BOARDENTRY_ON_PLAYER_INFO FOREIGN KEY (player_info_id) REFERENCES player_info (id);

ALTER TABLE player_info_racenets
    ADD CONSTRAINT fk_playerinfo_racenets_on_player_info FOREIGN KEY (player_info_id) REFERENCES player_info (id);

ALTER TABLE player_info
DROP
COLUMN created_before_racenet_change;

ALTER TABLE player_info
DROP
COLUMN is_outdated;

ALTER TABLE player_info
DROP
COLUMN platform_name;

insert into player_info_racenets(player_info_id, racenets) select id, racenet from player_info;