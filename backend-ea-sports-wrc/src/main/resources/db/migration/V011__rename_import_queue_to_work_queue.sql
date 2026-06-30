-- The worker queue started life import-only (import_target / import_job) but now also
-- runs non-import maintenance jobs (e.g. Discord config cleanup). Rename the tables,
-- sequences, constraints and indexes to the generic "job" vocabulary used in code:
--   import_target -> job_target   (the durable schedule: what recurs, how often)
--   import_job    -> job          (the throwaway work queue: do this one thing now)
-- Pure renames: no data is moved and sequence values are preserved.

ALTER TABLE import_target RENAME TO job_target;
ALTER TABLE import_job RENAME TO job;

ALTER SEQUENCE import_target_seq RENAME TO job_target_seq;
ALTER SEQUENCE import_job_seq RENAME TO job_seq;

ALTER TABLE job_target RENAME CONSTRAINT pk_import_target TO pk_job_target;
ALTER TABLE job_target RENAME CONSTRAINT uq_import_target_type_ref TO uq_job_target_type_ref;
ALTER TABLE job RENAME CONSTRAINT pk_import_job TO pk_job;

ALTER INDEX idx_import_target_due RENAME TO idx_job_target_due;
ALTER INDEX idx_import_job_claim RENAME TO idx_job_claim;
