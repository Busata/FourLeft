CREATE TABLE linked_identity
(
    id               UUID                        NOT NULL,
    provider         VARCHAR(30)                 NOT NULL,
    provider_user_id VARCHAR(190)                NOT NULL,
    user_id          UUID                        NOT NULL,
    linked_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_linked_identity PRIMARY KEY (id),
    CONSTRAINT fk_linked_identity_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);

-- One external identity maps to exactly one account. This unique constraint is the
-- anti-abuse anchor: a banned user can't re-register and re-attach the same Steam id.
CREATE UNIQUE INDEX ux_linked_identity_provider_user ON linked_identity (provider, provider_user_id);

-- A given account holds at most one identity per provider (one Steam per user).
CREATE UNIQUE INDEX ux_linked_identity_user_provider ON linked_identity (user_id, provider);
