-- Backs the player reverse-lookup (the time-trial "profile" page): find every board a player appears
-- on by display name. Without this, that lookup scans the whole entry table.
CREATE INDEX idx_time_trial_entry_display_name ON time_trial_entry (display_name);
