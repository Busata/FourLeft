-- MIXED channels lock each driver to their home class (the class of their first run in the championship).
-- Additive: the previous build ignores the column. Revert SQL lives in docs/multi-club-channels.md.
ALTER TABLE discord_club_configuration
    ADD COLUMN class_lock_mode VARCHAR(32) NOT NULL DEFAULT 'WARN';
