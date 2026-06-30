-- A small, inspectable worker system, split into two concepts:
--   import_target = "what should be kept imported, and how often" (the durable schedule)
--   import_job    = "do this one thing now"                       (the throwaway work queue)
-- Both are claimed with SELECT ... FOR UPDATE SKIP LOCKED so multiple app
-- instances / threads can safely pull work without stepping on each other.

CREATE SEQUENCE IF NOT EXISTS import_target_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS import_job_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE import_target
(
    id                BIGINT       NOT NULL,
    type              VARCHAR(255) NOT NULL,            -- CLUB | TT
    ref               VARCHAR(255) NOT NULL,            -- clubId, or a TT combo key
    interval_sec      INTEGER      NOT NULL,            -- current cadence
    min_interval_sec  INTEGER      NOT NULL,            -- adaptive floor (== max => fixed cadence)
    max_interval_sec  INTEGER      NOT NULL,            -- adaptive ceiling
    next_run_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    enabled           BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_import_target PRIMARY KEY (id),
    CONSTRAINT uq_import_target_type_ref UNIQUE (type, ref)
);

CREATE INDEX idx_import_target_due ON import_target (enabled, next_run_at);

CREATE TABLE import_job
(
    id          BIGINT       NOT NULL,
    type        VARCHAR(255) NOT NULL,                  -- CLUB | TT
    ref         VARCHAR(255) NOT NULL,                  -- what to import
    status      VARCHAR(255) NOT NULL DEFAULT 'PENDING',-- PENDING | RUNNING | DONE | FAILED
    attempts    INTEGER      NOT NULL DEFAULT 0,
    run_after   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    locked_at   TIMESTAMP WITH TIME ZONE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    target_id   BIGINT,                                 -- originating recurring target, null for ad-hoc
    last_error  TEXT,
    CONSTRAINT pk_import_job PRIMARY KEY (id)
);

-- The worker's hot path: cheapest possible "what's claimable next".
CREATE INDEX idx_import_job_claim ON import_job (status, run_after);
