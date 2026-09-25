-- A MIXED channel posts one merged event-ended / championship-started message for all its clubs, but each
-- club reports the moment on its own (import threads run in parallel). Every club's arrival is recorded
-- here; the arrival that completes the set inserts the claim row (club_id '*') and posts — the primary key
-- makes that claim succeed exactly once.
CREATE TABLE discord_channel_post_gate
(
    channel_id BIGINT       NOT NULL,
    post_key   VARCHAR(255) NOT NULL,
    club_id    VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT pk_discord_channel_post_gate PRIMARY KEY (channel_id, post_key, club_id)
);
