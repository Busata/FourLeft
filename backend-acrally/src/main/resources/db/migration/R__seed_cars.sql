-- Repeatable seed of the base car catalogue. Idempotent (guarded by NOT EXISTS), so it re-applies
-- safely whenever this file's checksum changes; existing rows are never duplicated or overwritten,
-- and admin-created cars are left untouched.
INSERT INTO car (id, name, model_year, group_name, class_name, created_at)
SELECT gen_random_uuid(), v.name, v.model_year, v.group_name, v.class_name, now()
FROM (VALUES
    ('Delta Integrale Evoluzione', 1992, 'Group A',   'A8 EVO2'),
    ('124 Abarth Rally 16V',       1974, 'Group 2/4', 'H2'),
    ('A110 1.8',                   1973, 'Group 2/4', 'H2'),
    ('Impreza S3',                 1993, 'Group A',   'A8 EVO3'),
    ('i20 Rally2',                 2021, 'Group R',   'Rally2'),
    ('131 Abarth',                 1976, 'Group 2/4', 'H1'),
    ('208 Rally4',                 2020, 'Group R',   'Rally4'),
    ('306 II Maxi',                1997, 'Group A',   'K11'),
    ('Fabia RS Rally2',            2022, 'Group R',   'Rally2'),
    ('Fulvia Coupé HF',            1970, 'Group 2/4', 'H3'),
    ('GTA 1300 Junior',            1972, 'Group 2/4', 'H3'),
    ('Mini Cooper S',              1964, 'Group 2/4', 'H3'),
    ('Rally 037 Evoluzione 2',     1984, 'Group B',   'B2'),
    ('Stratos HF',                 1976, 'Group 2/4', 'H1'),
    ('Xsara WRC',                  2003, 'Group WR',  'EVO2')
) AS v(name, model_year, group_name, class_name)
WHERE NOT EXISTS (SELECT 1 FROM car c WHERE LOWER(c.name) = LOWER(v.name));
