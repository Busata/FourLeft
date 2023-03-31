ALTER TABLE player_info
    ADD is_outdated BOOLEAN default false;

ALTER TABLE player_info
    ALTER COLUMN is_outdated SET NOT NULL;