-- Backs the driver autocomplete on the profile/compare page: case-insensitive substring matching on
-- display name (ILIKE '%q%'). A plain btree can't serve a leading-wildcard match, so add a trigram GIN
-- index over lower(display_name); the suggest query lowercases both sides to match it.
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_time_trial_entry_display_name_trgm
    ON time_trial_entry USING gin (lower(display_name) gin_trgm_ops);
