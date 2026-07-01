-- Remove the database-backed worker queue (introduced in V010, renamed in V011).
-- The feature was rolled back in code; drop the now-unused tables and sequences so
-- deployed databases don't keep orphaned objects around. Dropping the tables also
-- removes their indexes (idx_job_target_due, idx_job_claim) and constraints
-- (pk_job_target, uq_job_target_type_ref, pk_job). The sequences are standalone
-- (not column-owned), so they're dropped explicitly.

DROP TABLE IF EXISTS job;
DROP TABLE IF EXISTS job_target;

DROP SEQUENCE IF EXISTS job_seq;
DROP SEQUENCE IF EXISTS job_target_seq;
