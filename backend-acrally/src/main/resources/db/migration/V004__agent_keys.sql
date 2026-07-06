-- Personal API keys the acrally-agent authenticates with (Authorization: Bearer <key>).
-- Only the SHA-256 hash is stored; the plaintext is shown once, at issuance.
CREATE TABLE api_key
(
    id           UUID                        NOT NULL,
    user_id      UUID                        NOT NULL,
    token_hash   VARCHAR(64)                 NOT NULL,
    label        VARCHAR(120),
    created_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_used_at TIMESTAMP WITHOUT TIME ZONE,
    revoked_at   TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_api_key PRIMARY KEY (id),
    CONSTRAINT fk_api_key_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);
CREATE UNIQUE INDEX ux_api_key_token_hash ON api_key (token_hash);
CREATE INDEX ix_api_key_user ON api_key (user_id);

-- Device-authorization pairings (RFC 8628 style): the agent starts one, the user approves it
-- in the browser via the short user_code, then the agent exchanges its device_code for a key.
-- device_code is stored hashed (agent-held secret); user_code is the human-entered short code.
CREATE TABLE device_pairing
(
    id               UUID                        NOT NULL,
    device_code_hash VARCHAR(64)                 NOT NULL,
    user_code        VARCHAR(20)                 NOT NULL,
    status           VARCHAR(20)                 NOT NULL,
    label            VARCHAR(120),
    user_id          UUID,
    api_key_id       UUID,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    expires_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    approved_at      TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_device_pairing PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ux_device_pairing_device_code ON device_pairing (device_code_hash);
-- Not globally unique: user_code is only meaningful while PENDING, and the app regenerates on the
-- rare active collision. Indexed for the approve/lookup path.
CREATE INDEX ix_device_pairing_user_code ON device_pairing (user_code);
