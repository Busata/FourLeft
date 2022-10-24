ALTER TABLE tiered_event_vehicles
    DROP COLUMN vehicles;

ALTER TABLE tiered_event_vehicles
    ADD vehicles VARCHAR(255);