-- User-submitted problem reports from the companion agent ("submit issue"): a free-text
-- description plus the save game and agent log captured at submit time, for admin debugging.
-- Sizes are stored at insert so the admin list never has to touch the blobs.
CREATE TABLE issue_report
(
    id             UUID                        NOT NULL,
    user_id        UUID                        NOT NULL,
    description    TEXT                        NOT NULL,
    agent_version  VARCHAR(40),
    save_game      BYTEA,
    save_game_name VARCHAR(190),
    save_game_size INTEGER                     NOT NULL DEFAULT 0,
    agent_log      BYTEA,
    agent_log_name VARCHAR(190),
    agent_log_size INTEGER                     NOT NULL DEFAULT 0,
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_issue_report PRIMARY KEY (id),
    CONSTRAINT fk_issue_report_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);
CREATE INDEX ix_issue_report_created ON issue_report (created_at DESC);
