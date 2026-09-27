-- When a sync first saw the entry on its board, carried over across syncs (racenet gives no run timestamp).
-- A MIXED channel keeps only a driver's first run when they enter several of its classes. Rows from before
-- this migration stay NULL: they were already there, so they count as earlier than anything seen after.
ALTER TABLE club_leaderboard_entry ADD COLUMN first_seen_at TIMESTAMP;
