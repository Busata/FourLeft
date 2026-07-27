-- Restricts the "Time Trial • Target times" embed to discord-tracked players, mirroring the
-- requires_tracking filter on club results. Off by default; a channel opts in via its configuration.
ALTER TABLE discord_club_configuration
    ADD COLUMN time_trial_top_tracked_only BOOLEAN NOT NULL DEFAULT FALSE;
