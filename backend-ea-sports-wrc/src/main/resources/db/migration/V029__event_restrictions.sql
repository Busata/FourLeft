-- Event restriction rules per channel config, stored as a jsonb list (mirrors scoring_anchors).
-- A rule targets a specific event id OR a whole championship id (event-specific wins) and, for
-- VEHICLE_ALLOWLIST, lists the allowed vehicle strings (exact match against
-- club_leaderboard_entry.vehicle). display_mode controls the results view (WARN badge vs EXCLUDE +
-- re-rank); scoring_mode controls custom-scoring points (EXCLUDE: violator scores 0 and compliant
-- drivers move up; PENALTY: flat deduction, floored at 0). Scoring modes only take effect when
-- custom_scoring_enabled is on — racenet's own standings cannot be altered.
ALTER TABLE discord_club_configuration
    ADD COLUMN event_restrictions JSONB;
