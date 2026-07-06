-- A driving session opened by the agent (server-issued id). Telemetry heartbeats update the
-- live snapshot columns; the run ends as COMPLETED (a result arrived), ABORTED, or STALE (swept).
CREATE TABLE agent_session
(
    id                UUID                        NOT NULL,
    user_id           UUID                        NOT NULL,
    api_key_id        UUID                        NOT NULL,
    driver            VARCHAR(190),
    car               VARCHAR(190),
    stage             VARCHAR(190),
    track             VARCHAR(190),
    started_at_ms     BIGINT,
    agent_version     VARCHAR(40),
    status            VARCHAR(20)                 NOT NULL,
    abort_reason      VARCHAR(40),
    last_heartbeat_at TIMESTAMP WITHOUT TIME ZONE,
    current_ms        INTEGER,
    speed_kmh         DOUBLE PRECISION,
    distance_m        DOUBLE PRECISION,
    created_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_agent_session PRIMARY KEY (id),
    CONSTRAINT fk_agent_session_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);
CREATE INDEX ix_agent_session_user ON agent_session (user_id, created_at DESC);

-- Authoritative penalised results. De-duplicated by the save-file tick per the API contract:
-- retries re-deliver the same result, so (user_id, timestamp_ticks) is the idempotency key.
CREATE TABLE stage_result
(
    id              UUID                        NOT NULL,
    session_id      UUID                        NOT NULL,
    user_id         UUID                        NOT NULL,
    stage           VARCHAR(190),
    car             VARCHAR(190),
    driver          VARCHAR(190),
    raw_ms          INTEGER                     NOT NULL,
    penalty_ms      INTEGER                     NOT NULL,
    total_ms        INTEGER                     NOT NULL,
    timestamp_ticks BIGINT                      NOT NULL,
    agent_version   VARCHAR(40),
    created_at      TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_stage_result PRIMARY KEY (id),
    CONSTRAINT fk_stage_result_session FOREIGN KEY (session_id) REFERENCES agent_session (id)
);
CREATE UNIQUE INDEX ux_stage_result_dedupe ON stage_result (user_id, timestamp_ticks);
CREATE INDEX ix_stage_result_user ON stage_result (user_id, created_at DESC);
