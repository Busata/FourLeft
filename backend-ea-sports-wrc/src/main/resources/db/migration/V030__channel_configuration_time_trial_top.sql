-- Per-channel toggle for the "Time Trial • Target times" embed posted alongside a new-event post.
-- Off by default; a channel opts in via its configuration.
ALTER TABLE discord_club_configuration
    ADD COLUMN time_trial_top_enabled BOOLEAN NOT NULL DEFAULT FALSE;
