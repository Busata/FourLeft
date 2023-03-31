CREATE TABLE discord_integration_access_token
(
    id            UUID NOT NULL,
    user_name     VARCHAR(255),
    access_token  VARCHAR(255),
    refresh_token VARCHAR(255),
    expire_date   TIMESTAMP WITHOUT TIME ZONE,
    scope         VARCHAR(255),
    CONSTRAINT pk_discordintegrationaccesstoken PRIMARY KEY (id)
);