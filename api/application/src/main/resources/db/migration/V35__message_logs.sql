ALTER TABLE message_log
    ADD view_type VARCHAR(255);

ALTER TABLE message_log
DROP
COLUMN author;

ALTER TABLE message_log
DROP
COLUMN content;