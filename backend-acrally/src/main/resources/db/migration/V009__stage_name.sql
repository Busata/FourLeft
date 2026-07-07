-- A catalogue mapping the raw stage identifier that arrives on results (stage_result.stage)
-- to a human-readable display name. Admins "collect" distinct raw names off the results into
-- this table and then give each a readable name; results render display_name when present,
-- falling back to the raw name until one is assigned.
CREATE TABLE stage_name
(
    id           UUID                        NOT NULL,
    raw_name     VARCHAR(300)                NOT NULL,
    display_name VARCHAR(300),
    created_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_stage_name PRIMARY KEY (id)
);
-- The raw name is the join key back to stage_result.stage, matched exactly, so it is unique.
CREATE UNIQUE INDEX ux_stage_name_raw ON stage_name (raw_name);
