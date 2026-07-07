-- A community club: a named group with a description and an optional social link.
-- Created by a signed-in user; the creator is retained for attribution/moderation.
CREATE TABLE club
(
    id          UUID                        NOT NULL,
    name        VARCHAR(120)                NOT NULL,
    description VARCHAR(2000),
    social_link VARCHAR(300),
    created_by  UUID                        NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_club PRIMARY KEY (id),
    CONSTRAINT fk_club_created_by FOREIGN KEY (created_by) REFERENCES app_user (id)
);
-- Club names are unique case-insensitively (mirrors the app_user display-name rule).
CREATE UNIQUE INDEX ux_club_name ON club (LOWER(name));
CREATE INDEX ix_club_created_at ON club (created_at DESC);
