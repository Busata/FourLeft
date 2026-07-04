-- Add vehicle class 22 "Rally3" to the time-trial catalog. Classes are denormalised into
-- time_trial_combination (one row per location·route·surface·class); every existing class covers the
-- full set of 504 location·route·surface combos, so the new class must too. Rather than hardcode 504
-- rows, derive them from the distinct combos already in the catalog — id keeps the catalog's
-- "{location_id}-{route_id}-{surface_condition}-{vehicle_class_id}" shape. ON CONFLICT keeps this
-- idempotent if a matching row somehow already exists.
INSERT INTO time_trial_combination (id, location_id, location, route_id, route, surface_condition,
                                    vehicle_class_id, vehicle_class)
SELECT location_id || '-' || route_id || '-' || surface_condition || '-22',
       location_id, location, route_id, route, surface_condition, 22, 'Rally3'
FROM (SELECT DISTINCT location_id, location, route_id, route, surface_condition
      FROM time_trial_combination) combos
ON CONFLICT (id) DO NOTHING;
