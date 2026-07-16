-- Maps the track string live telemetry reports (arrives on agent_session.track) to a variant.
-- Telemetry names are display names, per-variant and localized ("Alsace Montée" is Munster Full
-- Forward, "Alsace Descente" its reverse; a French client sends "Pays de Galles Hafren Forest"),
-- so they form their own namespace next to variant.raw_name (the save-file key). The mapping
-- decides whether a freshly opened session is a run of the armed stage and must bind — an
-- unassigned or unknown name errs toward binding, so gaps can never re-open discard-grinding.
-- Aliases are collected from sessions like car aliases are from results; an admin assigns each.
CREATE TABLE track_alias
(
    id         UUID                        NOT NULL,
    raw_name   VARCHAR(190)                NOT NULL,
    variant_id UUID,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_track_alias PRIMARY KEY (id),
    CONSTRAINT fk_track_alias_variant FOREIGN KEY (variant_id) REFERENCES variant (id) ON DELETE SET NULL
);
-- The raw name is the exact join key back from agent_session.track, so it is unique.
CREATE UNIQUE INDEX ux_track_alias_raw ON track_alias (raw_name);
CREATE INDEX ix_track_alias_variant ON track_alias (variant_id);

-- Backfill from history: every telemetry track name ever seen becomes an alias, assigned only
-- where all of its completed sessions agree on one variant. Late/recovered records get attached
-- to whatever session was still around, so a name can carry contradictory pairings ("Livigno
-- Circuit Main Reverse" observed with Alsace results) — those stay unassigned for admin review
-- rather than poisoning the mapping.
INSERT INTO track_alias (id, raw_name, variant_id, created_at)
SELECT gen_random_uuid(), observed.raw_name, observed.variant_id, now()
FROM (
    SELECT s.track                                                              AS raw_name,
           CASE WHEN count(DISTINCT v.id) = 1 THEN min(v.id::text)::uuid END    AS variant_id
    FROM agent_session s
    LEFT JOIN stage_result r ON r.session_id = s.id
    LEFT JOIN variant v ON v.raw_name = r.stage
    WHERE s.track IS NOT NULL AND btrim(s.track) <> ''
    GROUP BY s.track
) observed;

-- Older agents reported the raw variant key itself as the track name — those map to themselves.
UPDATE track_alias ta
SET variant_id = v.id, updated_at = now()
FROM variant v
WHERE ta.variant_id IS NULL AND v.raw_name = ta.raw_name;

-- Same for telemetry car names ("Mini Cooper S 1275", "Alpine_A110"): they join the existing
-- car_alias namespace so one resolver serves both result and telemetry strings. Assignment is
-- resolved through the aliases the results already carry, again only when unanimous.
INSERT INTO car_alias (id, raw_name, car_id, created_at)
SELECT gen_random_uuid(), observed.raw_name, observed.car_id, now()
FROM (
    SELECT s.car                                                                    AS raw_name,
           CASE WHEN count(DISTINCT ca.car_id) = 1 THEN min(ca.car_id::text)::uuid END AS car_id
    FROM agent_session s
    LEFT JOIN stage_result r ON r.session_id = s.id
    LEFT JOIN car_alias ca ON ca.raw_name = r.car AND ca.car_id IS NOT NULL
    WHERE s.car IS NOT NULL AND btrim(s.car) <> ''
    GROUP BY s.car
) observed
WHERE NOT EXISTS (SELECT 1 FROM car_alias existing WHERE existing.raw_name = observed.raw_name);
