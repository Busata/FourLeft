-- One-off cleanup of duplicate leaderboard rows: Racenet pins the authenticated fetch account's own
-- entry into every page, so earlier fetches stored that player once per page walked — many identical
-- rows per board, all sharing the same fetched_at (so the latest-generation read filter couldn't
-- collapse them). The fetcher now dedupes by player key while storing; this removes the rows already
-- written. Keep one row per (board, generation, player identity), matching the churn/read key
-- coalesce(ssid, wrc_player_id, display_name).
DELETE FROM time_trial_entry
WHERE ctid IN (
    SELECT ctid
    FROM (
        SELECT ctid,
               row_number() OVER (
                   PARTITION BY combination_id, fetched_at,
                                coalesce(ssid, wrc_player_id, display_name)
                   ORDER BY ctid
               ) AS rn
        FROM time_trial_entry
    ) ranked
    WHERE ranked.rn > 1
);
