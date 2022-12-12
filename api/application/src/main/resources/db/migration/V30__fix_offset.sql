ALTER TABLE fixed_points_calculator
DROP
COLUMN offset_championship;

ALTER TABLE fixed_points_calculator
    ADD offset_championship VARCHAR(255);

update fixed_points_calculator set offset_championship='684767' where join_championships_count=4;