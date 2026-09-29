-- Private links to the operator pages (profile disputes, for now), like tier_set_link: the UUID is the credential.
-- Minted by the backend into the admin log channel, or by devops/cli/disputes.sh. They expire, since the pages
-- show discord ids across servers. Timestamps are UTC, like ApplicationClock. Purely additive; revert: DROP TABLE admin_link;
CREATE TABLE admin_link
(
    id         UUID         NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    expires_at TIMESTAMP    NOT NULL,
    CONSTRAINT pk_admin_link PRIMARY KEY (id)
);
