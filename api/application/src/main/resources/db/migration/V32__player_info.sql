CREATE TABLE player_info
(
    id              UUID    NOT NULL,
    racenet         VARCHAR(255),
    platform        VARCHAR(255),
    controller      VARCHAR(255),
    synced_platform BOOLEAN NOT NULL,
    CONSTRAINT pk_playerinfo PRIMARY KEY (id)
);

ALTER TABLE player_info
    ADD CONSTRAINT uc_playerinfo_racenet UNIQUE (racenet);