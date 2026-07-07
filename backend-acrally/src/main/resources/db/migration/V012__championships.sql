-- Championship scheduling for clubs. A club owner schedules a championship (a name + a start
-- date); it holds an ordered set of events. Each event runs an ordered list of variants (the
-- drivable routes that results key on) and permits a set of cars. Event calendar dates are NOT
-- stored — they are derived from the championship start plus each event's gap/duration, so editing
-- one event's length cascades to the rest.

-- A championship belongs to a club; the club's creator (owner) manages it.
CREATE TABLE championship
(
    id         UUID                        NOT NULL,
    club_id    UUID                        NOT NULL,
    name       VARCHAR(120)                NOT NULL,
    start_date DATE                        NOT NULL,
    status     VARCHAR(20)                 NOT NULL DEFAULT 'DRAFT',
    created_by UUID                        NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_championship PRIMARY KEY (id),
    CONSTRAINT fk_championship_club FOREIGN KEY (club_id) REFERENCES club (id) ON DELETE CASCADE,
    CONSTRAINT fk_championship_created_by FOREIGN KEY (created_by) REFERENCES app_user (id)
);
CREATE INDEX ix_championship_club ON championship (club_id);

-- An ordered round within a championship. `position` gives the running order; `gap_days` is the
-- pause before it opens (from the championship start for the first event, otherwise from the prior
-- event's close), and `duration_days` is how long it stays open.
CREATE TABLE championship_event
(
    id               UUID                        NOT NULL,
    championship_id  UUID                        NOT NULL,
    name             VARCHAR(120)                NOT NULL,
    position         INTEGER                     NOT NULL,
    gap_days         INTEGER                     NOT NULL DEFAULT 0,
    duration_days    INTEGER                     NOT NULL DEFAULT 7,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_championship_event PRIMARY KEY (id),
    CONSTRAINT fk_championship_event_championship FOREIGN KEY (championship_id) REFERENCES championship (id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX ux_championship_event_position ON championship_event (championship_id, position);

-- The ordered variants (drivable routes) run in an event. Order is the stage sequence within the
-- event; each variant appears once.
CREATE TABLE event_variant
(
    id         UUID    NOT NULL,
    event_id   UUID    NOT NULL,
    variant_id UUID    NOT NULL,
    position   INTEGER NOT NULL,
    CONSTRAINT pk_event_variant PRIMARY KEY (id),
    CONSTRAINT fk_event_variant_event FOREIGN KEY (event_id) REFERENCES championship_event (id) ON DELETE CASCADE,
    CONSTRAINT fk_event_variant_variant FOREIGN KEY (variant_id) REFERENCES variant (id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX ux_event_variant_position ON event_variant (event_id, position);
CREATE UNIQUE INDEX ux_event_variant_unique ON event_variant (event_id, variant_id);

-- The cars permitted in an event (a snapshot of car ids; "all" / "all in group" are UI helpers that
-- populate this explicit list). Unordered.
CREATE TABLE event_car
(
    id       UUID NOT NULL,
    event_id UUID NOT NULL,
    car_id   UUID NOT NULL,
    CONSTRAINT pk_event_car PRIMARY KEY (id),
    CONSTRAINT fk_event_car_event FOREIGN KEY (event_id) REFERENCES championship_event (id) ON DELETE CASCADE,
    CONSTRAINT fk_event_car_car FOREIGN KEY (car_id) REFERENCES car (id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX ux_event_car_unique ON event_car (event_id, car_id);
