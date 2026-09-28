-- Tier sets: named, ordered tiers ("JRC 1", "JRC 2", ...) with players assigned to them. Standalone for now;
-- attaching a set to a channel / its clubs comes later. Purely additive — revert SQL in docs/tier-sets.md.
CREATE TABLE tier_set
(
    id         UUID         NOT NULL,
    name       VARCHAR(100) NOT NULL,
    -- Discord guild the set was created from (scopes /fourleft tiers edit); NULL when created from the CLI.
    guild_id   BIGINT,
    created_by VARCHAR(255),
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT pk_tier_set PRIMARY KEY (id)
);

CREATE INDEX idx_tier_set_guild_id ON tier_set (guild_id);

CREATE TABLE tier
(
    id          UUID         NOT NULL,
    tier_set_id UUID         NOT NULL,
    position    INTEGER      NOT NULL,
    label       VARCHAR(100) NOT NULL,
    CONSTRAINT pk_tier PRIMARY KEY (id),
    CONSTRAINT fk_tier_tier_set FOREIGN KEY (tier_set_id) REFERENCES tier_set (id) ON DELETE CASCADE
);

CREATE INDEX idx_tier_tier_set_id ON tier (tier_set_id);

-- player_id is the racenet ssid (club_leaderboard_entry.ssid); display_name is the name at assignment time.
-- A player sits in at most one tier of a set — enforced by the application (moving = remove + add).
CREATE TABLE tier_player
(
    tier_id      UUID         NOT NULL,
    player_id    VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    CONSTRAINT pk_tier_player PRIMARY KEY (tier_id, player_id),
    CONSTRAINT fk_tier_player_tier FOREIGN KEY (tier_id) REFERENCES tier (id) ON DELETE CASCADE
);

-- Private edit links, like channel_configuration_request: the UUID is the credential.
CREATE TABLE tier_set_link
(
    id           UUID         NOT NULL,
    tier_set_id  UUID         NOT NULL,
    discord_id   VARCHAR(255),
    created_at   TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT pk_tier_set_link PRIMARY KEY (id),
    CONSTRAINT fk_tier_set_link_tier_set FOREIGN KEY (tier_set_id) REFERENCES tier_set (id) ON DELETE CASCADE
);
