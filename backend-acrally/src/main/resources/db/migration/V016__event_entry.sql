-- A recorded leaderboard time: the best penalised total a driver has set on one stage (variant) of
-- an event. Written by the ingestion pipeline when a run consumes a matching arm (see event_arm).
-- One row per (event, variant, driver) — the driver's best; a faster later run overwrites it. The
-- event's overall standings are derived on read (sum of a driver's best per stage).
CREATE TABLE event_entry
(
    id          UUID                        NOT NULL,
    event_id    UUID                        NOT NULL,
    variant_id  UUID                        NOT NULL,
    user_id     UUID                        NOT NULL,
    -- The catalogue car matched by name, when the raw car string resolved to one; the raw string is
    -- kept regardless so the board can always show what was driven.
    car_id      UUID,
    car_name    VARCHAR(190),
    -- The stage_result this entry currently reflects (updated when a faster time replaces it).
    result_id   UUID                        NOT NULL,
    raw_ms      INTEGER                     NOT NULL,
    penalty_ms  INTEGER                     NOT NULL,
    total_ms    INTEGER                     NOT NULL,
    recorded_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_event_entry PRIMARY KEY (id),
    CONSTRAINT fk_event_entry_event FOREIGN KEY (event_id) REFERENCES championship_event (id) ON DELETE CASCADE,
    CONSTRAINT fk_event_entry_variant FOREIGN KEY (variant_id) REFERENCES variant (id),
    CONSTRAINT fk_event_entry_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_event_entry_car FOREIGN KEY (car_id) REFERENCES car (id),
    CONSTRAINT fk_event_entry_result FOREIGN KEY (result_id) REFERENCES stage_result (id)
);
CREATE UNIQUE INDEX ux_event_entry_driver ON event_entry (event_id, variant_id, user_id);
CREATE INDEX ix_event_entry_board ON event_entry (event_id, variant_id, total_ms);
