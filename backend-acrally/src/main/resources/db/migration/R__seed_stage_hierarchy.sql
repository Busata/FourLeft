-- Repeatable seed of base locations and their stages. Idempotent (guarded by NOT EXISTS), so it
-- re-applies safely whenever this file's checksum changes. Variants are NOT seeded — they are
-- collected from results. Editing this file re-runs it; existing rows are never duplicated or
-- overwritten, and any admin-created locations/stages are left untouched.

-- Locations (name is unique case-insensitively).
INSERT INTO location (id, name, nation, created_at)
SELECT gen_random_uuid(), v.name, v.nation, now()
FROM (VALUES
    ('Alsace', 'France'),
    ('Greece', 'Greece'),
    ('Livigno Circuit', 'Italy'),
    ('Monte Carlo', 'Monaco'),
    ('Wales', 'Wales')
) AS v(name, nation)
WHERE NOT EXISTS (SELECT 1 FROM location l WHERE LOWER(l.name) = LOWER(v.name));

-- Stages, each attached to its location (name is unique per location).
INSERT INTO stage (id, name, location_id, created_at)
SELECT gen_random_uuid(), v.stage, l.id, now()
FROM (VALUES
    ('Alsace',          'Vallée de Munster'),
    ('Alsace',          'Saverne'),
    ('Greece',          'Elatia'),
    ('Greece',          'Loutraki'),
    ('Livigno Circuit', 'Ice Track'),
    ('Monte Carlo',     'Col de Turini'),
    ('Monte Carlo',     'Sisteron'),
    ('Wales',           'Hafren North'),
    ('Wales',           'Hafren South')
) AS v(location, stage)
JOIN location l ON LOWER(l.name) = LOWER(v.location)
WHERE NOT EXISTS (
    SELECT 1 FROM stage s WHERE s.location_id = l.id AND LOWER(s.name) = LOWER(v.stage)
);
