-- Archived Discord screenshots: one row per image attachment, message-level fields denormalized.
-- A multi-image Discord message shares message_id and is regrouped into one post by the feed query.
-- attachment_id is a globally unique Discord snowflake -> natural dedupe key, so the backfill can be
-- rerun and can race the live listener safely. Image bytes live in the external imagestore
-- (image_store_uuid); this table only holds metadata.
CREATE TABLE media_archive_entry
(
    id                  UUID                        NOT NULL,
    guild_id            BIGINT,
    channel_id          BIGINT                      NOT NULL,
    channel_name        VARCHAR(255),
    message_id          BIGINT                      NOT NULL,
    attachment_id       BIGINT                      NOT NULL,
    author_id           BIGINT,
    author_username     VARCHAR(255),
    author_display_name VARCHAR(255),
    author_avatar_url   VARCHAR(512),
    caption             TEXT,
    message_timestamp   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    image_store_uuid    UUID                        NOT NULL,
    width               INTEGER,
    height              INTEGER,
    file_name           VARCHAR(255),
    content_type        VARCHAR(100),
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_media_archive_entry PRIMARY KEY (id),
    CONSTRAINT uq_media_archive_entry_attachment UNIQUE (attachment_id)
);

CREATE INDEX idx_media_archive_entry_feed ON media_archive_entry (message_id DESC);
CREATE INDEX idx_media_archive_entry_channel ON media_archive_entry (channel_id);
