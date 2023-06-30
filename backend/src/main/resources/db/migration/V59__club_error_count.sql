ALTER TABLE club
    ADD error_count BIGINT;

UPDATE club
SET error_count = 0
WHERE error_count IS NULL;
ALTER TABLE club
    ALTER COLUMN error_count SET NOT NULL;
