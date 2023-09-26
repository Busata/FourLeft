CREATE TABLE tiered_event
(
    id           UUID NOT NULL,
    tier_id      UUID,
    challenge_id VARCHAR(255),
    event_id     VARCHAR(255),
    CONSTRAINT pk_tieredevent PRIMARY KEY (id)
);

CREATE TABLE tiered_event_vehicles
(
    tiered_event_id UUID NOT NULL,
    vehicles        INTEGER
);

ALTER TABLE tiered_event
    ADD CONSTRAINT FK_TIEREDEVENT_ON_TIER FOREIGN KEY (tier_id) REFERENCES tier (id);

ALTER TABLE tiered_event_vehicles
    ADD CONSTRAINT fk_tieredevent_vehicles_on_tiered_event FOREIGN KEY (tiered_event_id) REFERENCES tiered_event (id);