-- The save-file tick is stamped when the player ENTERS an event, and the game overwrites that
-- event's single record slot with each completed run — so two different runs legitimately share
-- (user_id, timestamp_ticks) with different times (observed live 2026-07-07: 4:52.888 then
-- 4:55.631 under one tick). Widen the idempotency key to include total_ms: exact retries still
-- de-dupe, distinct runs of the same event no longer collide.
DROP INDEX ux_stage_result_dedupe;
CREATE UNIQUE INDEX ux_stage_result_dedupe ON stage_result (user_id, timestamp_ticks, total_ms);
