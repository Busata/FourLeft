-- Repeatable seed of the full variant catalogue, extracted from the game files
-- (DT_TracksVariants data table + Game.locres localization, AC Rally 2026-07 build).
--
-- raw_name is the exact key that arrives on results — including Kunos's own quirks
-- ("WelesS3HafrenNorth" sic, Bollène's CutN keys pairing with SHORTN display names).
-- The "(Variant N)" suffix follows the data-table row order per stage, which matches the
-- in-game variant numbering everywhere we could verify (Munster's deliberately reversed
-- order included). One known discrepancy: the game orders Saverne Full Forward before Full
-- Reverse (V1/V2), while previously hand-entered rows had them swapped; hand-authored
-- display names are never overwritten, so review those two manually if desired.
--
-- Upsert policy, in the spirit of R__seed_stage_hierarchy: missing variants are inserted,
-- NULL or placeholder ("Variant 5") display names are replaced, missing stage assignments
-- are filled — anything an admin already named or assigned is left untouched.

INSERT INTO variant (id, raw_name, display_name, stage_id, created_at)
SELECT gen_random_uuid(), v.raw_name, v.display_name, s.id, now()
FROM (VALUES
    -- Alsace — Vallée de Munster
    ('AlsaceS2MunsterFullReverse',      'Vallée de Munster Descente (Variant 1)',    'Alsace',          'Vallée de Munster'),
    ('AlsaceS2MunsterFullForward',      'Vallée de Munster Montée (Variant 2)',      'Alsace',          'Vallée de Munster'),
    ('AlsaceS2MunsterShort1Reverse',    'Forêt de Munster (Variant 3)',              'Alsace',          'Vallée de Munster'),
    ('AlsaceS2MunsterShort1Forward',    'Luttenbach près Munster (Variant 4)',       'Alsace',          'Vallée de Munster'),
    ('AlsaceS2MunsterShort2Reverse',    'Col du petit Ballon (Variant 5)',           'Alsace',          'Vallée de Munster'),
    ('AlsaceS2MunsterShort2Forward',    'Sommet de Munster (Variant 6)',             'Alsace',          'Vallée de Munster'),
    -- Alsace — Saverne
    ('AlsaceS4SaverneFullForward',      'Forêt de Saverne (Variant 1)',              'Alsace',          'Saverne'),
    ('AlsaceS4SaverneFullReverse',      'Steigenbach (Variant 2)',                   'Alsace',          'Saverne'),
    ('AlsaceS4SaverneShort1Forward',    'Obersteigen (Variant 3)',                   'Alsace',          'Saverne'),
    ('AlsaceS4SaverneShort1Reverse',    'La traversée de La Mossig (Variant 4)',     'Alsace',          'Saverne'),
    -- Greece — Elatia
    ('GreeceS3ElatiaFullForward',       'Elatia - Zeli (Variant 1)',                 'Greece',          'Elatia'),
    ('GreeceS3ElatiaFullReverse',       'Zeli - Elatia (Variant 2)',                 'Greece',          'Elatia'),
    ('GreeceS3ElatiaCut1Forward',       'Elatia (Variant 3)',                        'Greece',          'Elatia'),
    ('GreeceS3ElatiaCut1Reverse',       'Elatia Reverse (Variant 4)',                'Greece',          'Elatia'),
    ('GreeceS3ElatiaCut2Forward',       'Zeli (Variant 5)',                          'Greece',          'Elatia'),
    ('GreeceS3ElatiaCut2Reverse',       'Zeli Reverse (Variant 6)',                  'Greece',          'Elatia'),
    -- Greece — Loutraki
    ('GreeceS4LoutrakiFullForward',     'Loutraki - Aghii Theodori (Variant 1)',     'Greece',          'Loutraki'),
    ('GreeceS4LoutrakiFullReverse',     'Aghii Theodori - Loutraki (Variant 2)',     'Greece',          'Loutraki'),
    ('GreeceS4LoutrakiCut1Forward',     'New Loutraki (Variant 3)',                  'Greece',          'Loutraki'),
    ('GreeceS4LoutrakiCut1Reverse',     'New Loutraki Reverse (Variant 4)',          'Greece',          'Loutraki'),
    ('GreeceS4LoutrakiCut2Forward',     'Aghii Theodori (Variant 5)',                'Greece',          'Loutraki'),
    ('GreeceS4LoutrakiCut2Reverse',     'Aghii Theodori Reverse (Variant 6)',        'Greece',          'Loutraki'),
    -- Livigno Circuit — Ice Track
    ('LivignoTestTrack01FullForward',   'Main Circuit (Variant 1)',                  'Livigno Circuit', 'Ice Track'),
    ('LivignoTestTrack01FullReverse',   'Main Circuit Reverse (Variant 2)',          'Livigno Circuit', 'Ice Track'),
    -- Monte Carlo — Col de Turini (raw keys use CutN; display names come from the SHORTN loc keys)
    ('MonteCarloS1BolleneFullForward',  'La Bollène-Vésubie - Peïra Cava (Variant 1)', 'Monte Carlo',   'Col de Turini'),
    ('MonteCarloS1BolleneFullReverse',  'Peïra Cava - La Bollène-Vésubie (Variant 2)', 'Monte Carlo',   'Col de Turini'),
    ('MonteCarloS1BolleneCut1Forward',  'La Bollène-Vésubie - Turini (Variant 3)',   'Monte Carlo',     'Col de Turini'),
    ('MonteCarloS1BolleneCut1Reverse',  'Turini - La Bollène-Vésubie (Variant 4)',   'Monte Carlo',     'Col de Turini'),
    ('MonteCarloS1BolleneCut2Forward',  'Turini - Peïra Cava (Variant 5)',           'Monte Carlo',     'Col de Turini'),
    ('MonteCarloS1BolleneCut2Reverse',  'Peïra Cava - Turini (Variant 6)',           'Monte Carlo',     'Col de Turini'),
    ('MonteCarloS1BolleneCut3Forward',  'Pra d''Alart (Variant 7)',                  'Monte Carlo',     'Col de Turini'),
    ('MonteCarloS1BolleneCut3Reverse',  'Sommet de Turini (Variant 8)',              'Monte Carlo',     'Col de Turini'),
    -- Monte Carlo — Sisteron
    ('MonteCarloS2SisteronFullForward', 'Sisteron - St. Geniez (Variant 1)',         'Monte Carlo',     'Sisteron'),
    ('MonteCarloS2SisteronFullReverse', 'St. Geniez - Sisteron (Variant 2)',         'Monte Carlo',     'Sisteron'),
    ('MonteCarloS2SisteronCut1Forward', 'Sisteron - Mézien (Variant 3)',             'Monte Carlo',     'Sisteron'),
    ('MonteCarloS2SisteronCut1Reverse', 'Mézien - Sisteron (Variant 4)',             'Monte Carlo',     'Sisteron'),
    ('MonteCarloS2SisteronCut2Forward', 'Mézien - St. Geniez (Variant 5)',           'Monte Carlo',     'Sisteron'),
    ('MonteCarloS2SisteronCut2Reverse', 'St. Geniez - Mézien (Variant 6)',           'Monte Carlo',     'Sisteron'),
    -- Wales — Hafren North ("Weles" sic: that is the key the game emits on results)
    ('WelesS3HafrenNorthFullForward',   'Cwmbiga - Afon Biga (Variant 1)',           'Wales',           'Hafren North'),
    ('WelesS3HafrenNorthFullReverse',   'Afon Biga - Cwmbiga (Variant 2)',           'Wales',           'Hafren North'),
    ('WelesS3HafrenNorthCut1Forward',   'Cwmbiga - Fedw Fain (Variant 3)',           'Wales',           'Hafren North'),
    ('WelesS3HafrenNorthCut1Reverse',   'Fedw Fain - Cwmbiga (Variant 4)',           'Wales',           'Hafren North'),
    ('WelesS3HafrenNorthCut2Forward',   'Banc Gwyn - Afon Biga (Variant 5)',         'Wales',           'Hafren North'),
    ('WelesS3HafrenNorthCut2Reverse',   'Afon Biga - Banc Gwyn (Variant 6)',         'Wales',           'Hafren North'),
    -- Wales — Hafren South
    ('WelesS4HafrenSouthFullForward',   'Afon Bidno - Severn (Variant 1)',           'Wales',           'Hafren South'),
    ('WelesS4HafrenSouthFullReverse',   'Severn - Afon Bidno (Variant 2)',           'Wales',           'Hafren South')
) AS v(raw_name, display_name, location_name, stage_name)
JOIN location l ON LOWER(l.name) = LOWER(v.location_name)
JOIN stage s ON s.location_id = l.id AND LOWER(s.name) = LOWER(v.stage_name)
ON CONFLICT (raw_name) DO UPDATE SET
    display_name = CASE
        WHEN variant.display_name IS NULL OR variant.display_name ~ '^Variant [0-9]+$'
        THEN EXCLUDED.display_name
        ELSE variant.display_name
    END,
    stage_id   = COALESCE(variant.stage_id, EXCLUDED.stage_id),
    updated_at = now()
WHERE variant.display_name IS NULL
   OR variant.display_name ~ '^Variant [0-9]+$'
   OR variant.stage_id IS NULL;
