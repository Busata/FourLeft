ALTER TABLE discord_club_configuration
    ADD requires_tracking BOOLEAN;

update discord_club_configuration set requires_tracking=false;

ALTER TABLE discord_club_configuration
    ALTER COLUMN requires_tracking SET NOT NULL;