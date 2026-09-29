-- Profile claims: how much we trust the discord ↔ racenet link, and who disputes it. Purely additive; existing
-- links become CLAIMED. Revert:
--   DROP INDEX idx_profile_disputed_by_discord_id;
--   DROP INDEX idx_profile_discord_id;
--   ALTER TABLE profile DROP COLUMN disputed_at, DROP COLUMN disputed_by_discord_id, DROP COLUMN claim_state;
ALTER TABLE profile
    ADD COLUMN claim_state VARCHAR(20) NOT NULL DEFAULT 'CLAIMED';

-- The discord user disputing the holder's claim (DISPUTED), and since when. One disputer per profile.
ALTER TABLE profile
    ADD COLUMN disputed_by_discord_id VARCHAR(255);
ALTER TABLE profile
    ADD COLUMN disputed_at TIMESTAMP;

CREATE INDEX idx_profile_discord_id ON profile (discord_id);
CREATE INDEX idx_profile_disputed_by_discord_id ON profile (disputed_by_discord_id);
