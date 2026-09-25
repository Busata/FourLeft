-- A channel can track several clubs (multi-class / multi-tier championships). The clubs move to a child
-- table ordered by position; position 0 is the channel's primary club, which every channel-scoped view
-- keeps reading until the MIXED/TIERED modes get their own rendering.
--
-- Additive only: discord_club_configuration.club_id is left in place and kept in sync with the primary
-- club by the application, so rolling back to the previous build needs no data repair. Revert SQL lives
-- in docs/multi-club-channels.md.
ALTER TABLE discord_club_configuration
    ADD COLUMN mode VARCHAR(32) NOT NULL DEFAULT 'SINGLE';

CREATE TABLE discord_club_configuration_club
(
    configuration_id UUID         NOT NULL,
    position         INTEGER      NOT NULL,
    club_id          VARCHAR(255) NOT NULL,
    label            VARCHAR(255),
    CONSTRAINT pk_discord_club_configuration_club PRIMARY KEY (configuration_id, position),
    -- Cascades so the JPQL bulk deletes on the parent (removeByChannelId) don't orphan rows.
    CONSTRAINT fk_discord_club_configuration_club_configuration FOREIGN KEY (configuration_id)
        REFERENCES discord_club_configuration (id) ON DELETE CASCADE
);

CREATE INDEX idx_discord_club_configuration_club_club_id ON discord_club_configuration_club (club_id);

INSERT INTO discord_club_configuration_club (configuration_id, position, club_id, label)
SELECT id, 0, club_id, NULL
FROM discord_club_configuration
WHERE club_id IS NOT NULL;
