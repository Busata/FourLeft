CREATE TABLE app_user
(
    id            UUID                        NOT NULL,
    email         VARCHAR(320)                NOT NULL,
    password_hash VARCHAR(100)                NOT NULL,
    display_name  VARCHAR(60)                 NOT NULL,
    status        VARCHAR(20)                 NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_app_user PRIMARY KEY (id)
);

-- Case-insensitive uniqueness: nobody registers "Foo@x.com" when "foo@x.com" exists,
-- and display names can't collide by case either (cheap impersonation guard until the
-- Steam anchor lands in Phase 2).
CREATE UNIQUE INDEX ux_app_user_email ON app_user (lower(email));
CREATE UNIQUE INDEX ux_app_user_display_name ON app_user (lower(display_name));
