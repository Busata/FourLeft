-- Steam-only sign-in: the password credential is gone and email becomes an optional,
-- user-volunteered contact field. Existing accounts keep working — every account able to
-- submit results already has a Steam identity linked (pairing requires it), and Steam
-- sign-in resolves through linked_identity, not email.
--
-- Revert (schema only — password hashes are secrets and are deliberately NOT backed up;
-- reverting would require users to set new passwords):
--   ALTER TABLE app_user ADD COLUMN password_hash VARCHAR(100);
--   ALTER TABLE app_user ALTER COLUMN email SET NOT NULL;

ALTER TABLE app_user
    DROP COLUMN password_hash;

ALTER TABLE app_user
    ALTER COLUMN email DROP NOT NULL;

-- ux_app_user_email (unique on lower(email)) stays: it still de-dupes real addresses,
-- and NULLs don't collide in a Postgres unique index.
