-- The time-trial catalog (V017) is immutable reference data. Whether a board exists is a probe
-- observation, now recorded as append-only history in time_trial_probe (V018) — the latest row per
-- combination is the current state. The catalog's 'valid' column was never populated (always NULL)
-- and is superseded, so drop it (its index idx_time_trial_valid is dropped with it).
ALTER TABLE time_trial_combination DROP COLUMN valid;
