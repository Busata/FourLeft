-- Migrate club 146 from the LOOKUP_TABLE strategy (a 1000-row position->points map seeded in V027) to the
-- compact POINT_ANCHOR strategy: an ordered list of anchors (position pinned to an absolute value) and
-- decreases (points lost per position, possibly fractional, until the next entry). Positions covered by
-- neither score `floor` (1) -- matching the old table's "unlisted position scores 1" behaviour.
--
-- The 11 entries below reproduce the old table's points 1..203 byte-for-byte, then approximate the tail
-- (a rounded curve) with two averaged decreases joined by a midpoint anchor at position 788. Deep-rank
-- deviation is <=17 points (positions 788 and 1000 land exactly; ranks 1001+ floor to 1) -- invisible for
-- any realistic field. Full analysis + the legacy table live in docs/scoring/club-146-legacy-lookup-table.md.
--
-- scoring_table is intentionally LEFT INTACT for club 146: it is ignored under POINT_ANCHOR, but keeping it
-- means rollback is a one-liner with no data to restore --
--   UPDATE discord_club_configuration SET scoring_strategy = 'LOOKUP_TABLE' WHERE club_id = '146';
ALTER TABLE discord_club_configuration
    ADD COLUMN scoring_anchors JSONB;

UPDATE discord_club_configuration
SET scoring_strategy = 'POINT_ANCHOR',
    scoring_anchors  = '{"floor":1,"entries":[
        {"position":1,"points":2500},
        {"position":2,"points":2200},
        {"position":3,"decrease":100},
        {"position":4,"points":2025},
        {"position":5,"decrease":25},
        {"position":7,"decrease":15},
        {"position":10,"decrease":5},
        {"position":26,"decrease":2},
        {"position":204,"decrease":1.83},
        {"position":788,"points":424},
        {"position":789,"decrease":1.99}
    ]}'::jsonb
WHERE club_id = '146';
