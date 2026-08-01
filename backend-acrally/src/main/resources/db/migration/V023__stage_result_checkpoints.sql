-- Sector splits, decoded from the save file's per-record checkpoint table (agent v0.4.2+).
-- Stored as a comma-separated list of cumulative millisecond times, finish included — the last
-- value equals raw_ms. NULL for results from older agents or records whose table didn't validate.
-- Longest known stage has 5 checkpoints; the column leaves headroom.
ALTER TABLE stage_result ADD COLUMN checkpoints_ms VARCHAR(512);
