-- Index for the results/standings/summary read hot path.
--
-- ClubLeaderboard.entries is a unidirectional @OneToMany joined on club_leaderboard_entry.leaderboard_id
-- (ClubLeaderboardRepository.findEntries / findAllWithEntriesByIds, and the lazy board.getEntries() load).
-- These run once per event while building results, previous results, standings and the event summary --
-- and again on every Discord MessageCache rebuild (fired on each ProfileUpdated/ConfigurationUpdated event).
--
-- club_leaderboard_entry had only its primary key (id), so every entries-by-leaderboard lookup was a
-- parallel seq scan of the whole table (~3M rows, ~94k buffers / ~735 MB read, ~1M rows discarded by
-- filter per worker, ~325 ms) to return the few thousand rows of one leaderboard. This table showed the
-- second-highest seq_tup_read in pg_stat_user_tables (127 trillion tuples). The index turns each lookup
-- into an index scan.
--
-- IF NOT EXISTS so this is a no-op if the index was already created live via CREATE INDEX CONCURRENTLY
-- as an out-of-band hotfix.

CREATE INDEX IF NOT EXISTS idx_club_leaderboard_entry_leaderboard_id ON club_leaderboard_entry (leaderboard_id);
