ALTER TABLE auto_post_entry
    ADD event_key VARCHAR(255);

DROP TABLE auto_post_tracking CASCADE;

DROP TABLE field_mapping CASCADE;

ALTER TABLE auto_post_entry
DROP
COLUMN challenge_id;

ALTER TABLE auto_post_entry
DROP
COLUMN event_id;

ALTER TABLE auto_post_entry
    ALTER COLUMN message_id DROP NOT NULL;