-- Introduces a Location → Stage → Variant hierarchy for stage naming.
--
--   * location  — a rally location (Greece, Monte Carlo, …) with its nation. Admin-managed.
--   * stage     — a stage within a location. Admin-managed, optionally assigned to a location.
--   * variant   — the unique key that arrives on results (stage_result.stage). Collected from
--                 results and optionally assigned to a stage. This is the repurposed stage_name.
--
-- Locations and stages are authored by hand; only variants are "collected". Results resolve
-- their raw key up the chain (variant → stage → location) to render a readable name.

CREATE TABLE location
(
    id         UUID                        NOT NULL,
    name       VARCHAR(200)                NOT NULL,
    nation     VARCHAR(120),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_location PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ux_location_name ON location (LOWER(name));

CREATE TABLE stage
(
    id          UUID                        NOT NULL,
    name        VARCHAR(200)                NOT NULL,
    location_id UUID,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_stage PRIMARY KEY (id),
    CONSTRAINT fk_stage_location FOREIGN KEY (location_id) REFERENCES location (id)
);
-- Stage names are unique per location (NULLs are distinct, so unassigned stages aren't constrained).
CREATE UNIQUE INDEX ux_stage_location_name ON stage (location_id, LOWER(name));

-- Repurpose the deployed stage_name table into `variant`, preserving collected rows and any
-- display names already assigned, and add the stage assignment.
ALTER TABLE stage_name RENAME TO variant;
ALTER TABLE variant RENAME CONSTRAINT pk_stage_name TO pk_variant;
ALTER INDEX ux_stage_name_raw RENAME TO ux_variant_raw;
ALTER TABLE variant ADD COLUMN stage_id UUID;
ALTER TABLE variant ADD CONSTRAINT fk_variant_stage FOREIGN KEY (stage_id) REFERENCES stage (id);
