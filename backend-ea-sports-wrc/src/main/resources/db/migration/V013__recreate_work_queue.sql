-- The DB-backed work queue, recreated after V012 dropped it. Two concepts:
--   job_target = "what we keep imported, and when it's next due"  (the durable schedule)
--   job        = "do this one thing now"                          (the throwaway work queue)
-- Both are claimed with SELECT ... FOR UPDATE SKIP LOCKED so multiple threads / instances
-- can pull work without stepping on each other.
--
-- Unlike the original queue (V010/V011, dropped in V012), job_target has no fixed-cadence
-- columns: next_run_at is DERIVED from each club's own domain state (ClubService.nextDueAt),
-- so idle clubs drift out on their own instead of being polled on a uniform timer.

CREATE SEQUENCE IF NOT EXISTS job_target_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS job_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE job_target
(
    id          BIGINT       NOT NULL,
    type        VARCHAR(255) NOT NULL,                         -- CLUB
    ref         VARCHAR(255) NOT NULL,                         -- clubId
    next_run_at TIMESTAMP    NOT NULL DEFAULT (now() AT TIME ZONE 'utc'), -- state-derived due time (app UTC clock)
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_job_target PRIMARY KEY (id),
    CONSTRAINT uq_job_target_type_ref UNIQUE (type, ref)
);

CREATE INDEX idx_job_target_due ON job_target (enabled, next_run_at);

-- A job runs exactly once: success -> DONE, failure -> FAILED (recorded, not retried). A club's
-- normal schedule brings it back around next cycle, so there is no per-job retry/backoff.
CREATE TABLE job
(
    id         BIGINT       NOT NULL,
    type       VARCHAR(255) NOT NULL,                          -- CLUB
    ref        VARCHAR(255) NOT NULL,                          -- what to import
    status     VARCHAR(255) NOT NULL DEFAULT 'PENDING',        -- PENDING | RUNNING | DONE | FAILED
    locked_at  TIMESTAMP WITH TIME ZONE,                       -- when a worker claimed it (for crash recovery)
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    target_id  BIGINT,                                         -- originating recurring target, null for ad-hoc
    last_error TEXT,
    CONSTRAINT pk_job PRIMARY KEY (id)
);

-- The worker's hot path: cheapest possible "what's claimable next".
CREATE INDEX idx_job_claim ON job (status, created_at);
