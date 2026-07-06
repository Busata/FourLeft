-- Snapshot of the Steam Web API profile for a linked Steam id. Keyed by steamID64 (the
-- STEAM linked_identity.provider_user_id). Refreshed on link and on login; all fields are
-- best-effort — a private profile omits account_created/persona, and the whole row is absent
-- when no Steam Web API key is configured.
CREATE TABLE steam_profile
(
    steam_id64       VARCHAR(20)                 NOT NULL,
    persona_name     VARCHAR(190),
    avatar_url       VARCHAR(500),
    profile_url      VARCHAR(500),
    account_created  TIMESTAMP WITHOUT TIME ZONE,
    visibility_state INT,
    vac_banned       BOOLEAN                     NOT NULL DEFAULT FALSE,
    game_ban_count   INT                         NOT NULL DEFAULT 0,
    community_banned BOOLEAN                     NOT NULL DEFAULT FALSE,
    fetched_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_steam_profile PRIMARY KEY (steam_id64)
);
