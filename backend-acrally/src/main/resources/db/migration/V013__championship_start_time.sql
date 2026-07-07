-- Championships now anchor on a precise start moment (date + time), not just a calendar date, so a
-- club can open a season at, say, Friday 18:00. Events still derive their windows from this start
-- plus each event's gap/duration in days, which now preserves the time-of-day. Rename
-- start_date -> starts_at and widen it to a timestamp (any existing rows land at midnight).
ALTER TABLE championship RENAME COLUMN start_date TO starts_at;
ALTER TABLE championship
    ALTER COLUMN starts_at TYPE TIMESTAMP WITHOUT TIME ZONE USING starts_at::timestamp;
