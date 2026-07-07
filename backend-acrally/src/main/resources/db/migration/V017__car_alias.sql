-- Maps the raw car string the game reports (arrives on stage_result.car and in live telemetry) to a
-- catalogue car. The game's name (e.g. "Lancia Delta Integrale Evo") often differs from the readable
-- catalogue name ("Lancia Delta HF Integrale"), so matching on the catalogue name alone misfires.
-- Aliases are collected from results like variants, then an admin assigns each to a car. Many aliases
-- can point at one car (the name varies across game versions); an unassigned alias has a null car_id.
CREATE TABLE car_alias
(
    id         UUID                        NOT NULL,
    raw_name   VARCHAR(190)                NOT NULL,
    car_id     UUID,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_car_alias PRIMARY KEY (id),
    CONSTRAINT fk_car_alias_car FOREIGN KEY (car_id) REFERENCES car (id) ON DELETE SET NULL
);
-- The raw name is the exact join key back from stage_result.car, so it is unique.
CREATE UNIQUE INDEX ux_car_alias_raw ON car_alias (raw_name);
CREATE INDEX ix_car_alias_car ON car_alias (car_id);
