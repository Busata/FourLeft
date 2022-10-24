CREATE TABLE player
(
    id      UUID NOT NULL,
    racenet VARCHAR(255),
    CONSTRAINT pk_player PRIMARY KEY (id)
);

CREATE TABLE player_tiers
(
    player_id UUID NOT NULL,
    tier_id   UUID NOT NULL
);