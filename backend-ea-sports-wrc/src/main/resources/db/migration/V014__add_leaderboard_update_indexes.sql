-- Indexes for ClubRepository.markBoardAsUpdated, the club-import hot path:
--   UPDATE event e SET last_leaderboard_update = :ts
--   WHERE e.leaderboard_id = :lb
--      OR :lb IN (SELECT s.leaderboardId FROM Event e2 JOIN e2.stages s WHERE e2.id = e.id)
--
-- Before this, the only indexes on these tables were the primary keys, so the correlated
-- subquery seq-scanned event (10.8k rows) and, per row, seq-scanned event_stages (54k rows)
-- -> ~half a billion row comparisons, ~20-30s per call. Five concurrent imports (work-queue
-- max-concurrent-jobs=5) each running one of these saturated Postgres, which starved every
-- other query (even the scheduler's lazy loads parked on socket reads) and wedged the queue.
--
-- idx_event_stages_event_id is the decisive one: it turns the per-event subquery into an index
-- lookup. The two leaderboard_id indexes support the outer predicate and the join's inner scan.
--
-- IF NOT EXISTS so this is a no-op if the indexes were already created live (e.g. via
-- CREATE INDEX CONCURRENTLY as an out-of-band hotfix).

CREATE INDEX IF NOT EXISTS idx_event_stages_event_id ON event_stages (event_id);
CREATE INDEX IF NOT EXISTS idx_event_leaderboard_id  ON event (leaderboard_id);
CREATE INDEX IF NOT EXISTS idx_stage_leaderboard_id  ON stage (leaderboard_id);
