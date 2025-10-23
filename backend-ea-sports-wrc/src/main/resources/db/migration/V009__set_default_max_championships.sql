UPDATE club_export_configuration
SET max_championships = 2
WHERE max_championships IS NULL;

ALTER TABLE club_export_configuration
    ALTER COLUMN max_championships SET DEFAULT 2;
