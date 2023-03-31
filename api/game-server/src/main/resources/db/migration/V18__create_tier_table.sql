CREATE TABLE tier
(
    id      UUID NOT NULL,
    club_id BIGINT,
    name    VARCHAR(255),
    CONSTRAINT pk_tier PRIMARY KEY (id)
);