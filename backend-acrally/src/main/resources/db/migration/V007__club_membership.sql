-- Membership of a user in a club. A user joins a club once (unique per club+user);
-- the club creator is auto-joined on creation. Leaving deletes the row.
CREATE TABLE club_membership
(
    id        UUID                        NOT NULL,
    club_id   UUID                        NOT NULL,
    user_id   UUID                        NOT NULL,
    joined_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_club_membership PRIMARY KEY (id),
    CONSTRAINT fk_club_membership_club FOREIGN KEY (club_id) REFERENCES club (id),
    CONSTRAINT fk_club_membership_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);
CREATE UNIQUE INDEX ux_club_membership ON club_membership (club_id, user_id);
CREATE INDEX ix_club_membership_user ON club_membership (user_id);
