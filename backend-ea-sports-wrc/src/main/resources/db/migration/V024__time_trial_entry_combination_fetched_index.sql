-- The profile reverse-lookup (every board a player is on) and the single-board view both filter each
-- candidate row to its board's latest generation with a correlated
--   fetched_at = (select max(fetched_at) from time_trial_entry where combination_id = <row's board>)
-- subquery. Backed only by the (combination_id) index, that max scans every stored row of the board
-- (up to ~1000) and aggregates — once per board. A player on hundreds of boards therefore drives
-- hundreds of thousands of row reads, which is the multi-second profile load.
--
-- Widen the index to (combination_id, fetched_at DESC): the max collapses to a single index seek (the
-- first entry per combination_id). combination_id stays the leftmost column, so this still serves the
-- whole-board load/delete that the old single-column index covered — making that index redundant.
CREATE INDEX idx_time_trial_entry_combination_fetched
    ON time_trial_entry (combination_id, fetched_at DESC);

DROP INDEX idx_time_trial_entry_combination;
