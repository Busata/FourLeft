-- Richer status reporting for completed jobs: what the run did, how long it took, how much it moved,
-- and whether it was recovered after a crashed worker. All columns are nullable (or defaulted) so
-- existing rows and in-flight jobs stay valid.
--
--   started_at / finished_at  -> run duration (finished - started) and queue wait (started - created).
--                                started_at is kept separate from locked_at, which is nulled on
--                                completion for crash recovery and so cannot carry the start time.
--   attempts                  -> worker starts; > 1 means requeue_stale recovered it at least once.
--   outcome                   -> JobOutcome: CLUB_CREATED / CHAMPIONSHIP_STARTED / EVENT_ENDED /
--                                LEADERBOARDS_UPDATED / HISTORY_UPDATED / DETAILS_REFRESHED /
--                                NO_CHANGE / SYNC_DISABLED.
--   changed                   -> whether the run altered stored data (vs a no-op refresh).
--   *_updated / entries_*     -> how much data moved this run.

ALTER TABLE job ADD COLUMN IF NOT EXISTS started_at           TIMESTAMP WITH TIME ZONE;
ALTER TABLE job ADD COLUMN IF NOT EXISTS finished_at          TIMESTAMP WITH TIME ZONE;
ALTER TABLE job ADD COLUMN IF NOT EXISTS attempts             INTEGER NOT NULL DEFAULT 0;
ALTER TABLE job ADD COLUMN IF NOT EXISTS outcome              VARCHAR(255);
ALTER TABLE job ADD COLUMN IF NOT EXISTS changed              BOOLEAN;
ALTER TABLE job ADD COLUMN IF NOT EXISTS leaderboards_updated INTEGER;
ALTER TABLE job ADD COLUMN IF NOT EXISTS standings_updated    INTEGER;
ALTER TABLE job ADD COLUMN IF NOT EXISTS entries_imported     INTEGER;
