CREATE SEQUENCE IF NOT EXISTS club_configuration_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE autopost_entry
(
    event_id   VARCHAR(255) NOT NULL,
    message_id BIGINT       NOT NULL,
    player_key VARCHAR(255) NOT NULL,
    channel_id BIGINT       NOT NULL,
    CONSTRAINT pk_autopostentry PRIMARY KEY (event_id, message_id, player_key, channel_id)
);

CREATE TABLE championship
(
    id                         VARCHAR(255) NOT NULL,
    absolute_open_date         TIMESTAMP WITHOUT TIME ZONE,
    absolute_close_date        TIMESTAMP WITHOUT TIME ZONE,
    status                     VARCHAR(255),
    club_id                    VARCHAR(255),
    updated_after_finish       BOOLEAN      NOT NULL,
    name                       VARCHAR(255),
    format                     BIGINT,
    bonus_points_mode          BIGINT,
    scoring_system             BIGINT,
    track_degradation          BIGINT,
    is_hardcore_damage_enabled BOOLEAN,
    is_assists_allowed         BOOLEAN,
    is_tuning_allowe           BOOLEAN,
    CONSTRAINT pk_championship PRIMARY KEY (id)
);

CREATE TABLE championship_standing
(
    id                          UUID NOT NULL,
    championship_id             VARCHAR(255),
    ssid                        VARCHAR(255),
    display_name                VARCHAR(255),
    points_accumulated          INTEGER,
    points_accumulated_previous INTEGER,
    rank                        INTEGER,
    previous_rank               INTEGER,
    nationality_id              INTEGER,
    CONSTRAINT pk_championship_standing PRIMARY KEY (id)
);

CREATE TABLE club
(
    id                      VARCHAR(255) NOT NULL,
    club_name               VARCHAR(255),
    club_description        VARCHAR(255),
    active_member_count     BIGINT,
    club_created_at         TIMESTAMP WITHOUT TIME ZONE,
    update_details_required BOOLEAN      NOT NULL,
    last_details_update     TIMESTAMP WITHOUT TIME ZONE,
    last_leaderboard_update TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_club PRIMARY KEY (id)
);

CREATE TABLE club_configuration
(
    id          BIGINT  NOT NULL,
    club_id     VARCHAR(255),
    keep_synced BOOLEAN NOT NULL,
    CONSTRAINT pk_clubconfiguration PRIMARY KEY (id)
);

CREATE TABLE club_leaderboard
(
    id            VARCHAR(255) NOT NULL,
    total_entries INTEGER      NOT NULL,
    CONSTRAINT pk_clubleaderboard PRIMARY KEY (id)
);

CREATE TABLE club_leaderboard_entry
(
    id                     UUID NOT NULL,
    ssid                   VARCHAR(255),
    display_name           VARCHAR(255),
    wrc_player_id          VARCHAR(255),
    rank                   BIGINT,
    rank_accumulated       BIGINT,
    nationalityid          BIGINT,
    platform               BIGINT,
    vehicle                VARCHAR(255),
    time                   BIGINT,
    difference_to_first    BIGINT,
    difference_accumulated BIGINT,
    time_accumulated       BIGINT,
    time_penalty           BIGINT,
    leaderboard_id         VARCHAR(255),
    CONSTRAINT pk_clubleaderboardentry PRIMARY KEY (id)
);

CREATE TABLE discord_club_configuration
(
    id                  UUID    NOT NULL,
    club_id             VARCHAR(255),
    channel_id          BIGINT,
    enabled             BOOLEAN NOT NULL,
    autoposting_enabled BOOLEAN NOT NULL,
    CONSTRAINT pk_discordclubconfiguration PRIMARY KEY (id)
);

CREATE TABLE event
(
    id                      VARCHAR(255) NOT NULL,
    leaderboard_id          VARCHAR(255),
    absolute_open_date      TIMESTAMP WITHOUT TIME ZONE,
    absolute_close_date     TIMESTAMP WITHOUT TIME ZONE,
    last_leaderboard_update TIMESTAMP WITHOUT TIME ZONE,
    championship_id         VARCHAR(255),
    status                  VARCHAR(255),
    vehicle_classid         BIGINT,
    vehicle_class           VARCHAR(255),
    weather_seasonid        BIGINT,
    weather_season          VARCHAR(255),
    locationid              BIGINT,
    location                VARCHAR(255),
    duration                VARCHAR(255),
    CONSTRAINT pk_event PRIMARY KEY (id)
);

CREATE TABLE event_stages
(
    event_id  VARCHAR(255) NOT NULL,
    stages_id VARCHAR(255) NOT NULL
);

CREATE TABLE field_mapping
(
    id             UUID    NOT NULL,
    name           VARCHAR(255),
    value          VARCHAR(255),
    type           VARCHAR(255),
    context        VARCHAR(255),
    note           VARCHAR(255),
    mapped_by_user BOOLEAN NOT NULL,
    CONSTRAINT pk_fieldmapping PRIMARY KEY (id)
);

CREATE TABLE profile
(
    id            VARCHAR(255) NOT NULL,
    display_name  VARCHAR(255),
    discord_id    VARCHAR(255),
    platform      VARCHAR(255),
    controller    VARCHAR(255),
    peripheral    VARCHAR(255),
    racenet       VARCHAR(255),
    track_discord BOOLEAN      NOT NULL,
    CONSTRAINT pk_profile PRIMARY KEY (id)
);

CREATE TABLE profile_update_request
(
    id                    UUID NOT NULL,
    discord_id            VARCHAR(255),
    requestedssid         VARCHAR(255),
    requested_update_time TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_profileupdaterequest PRIMARY KEY (id)
);

CREATE TABLE stage
(
    id                    VARCHAR(255) NOT NULL,
    leaderboard_id        VARCHAR(255),
    routeid               BIGINT,
    route                 VARCHAR(255),
    weather_and_surfaceid BIGINT,
    weather_and_surface   VARCHAR(255),
    time_of_dayid         BIGINT,
    time_of_day           VARCHAR(255),
    service_areaid        BIGINT,
    service_are           VARCHAR(255),
    CONSTRAINT pk_stage PRIMARY KEY (id)
);

ALTER TABLE event_stages
    ADD CONSTRAINT uc_event_stages_stages UNIQUE (stages_id);

ALTER TABLE profile
    ADD CONSTRAINT uc_profile_displayname UNIQUE (display_name);

ALTER TABLE championship
    ADD CONSTRAINT FK_CHAMPIONSHIP_ON_CLUB FOREIGN KEY (club_id) REFERENCES club (id);

ALTER TABLE championship_standing
    ADD CONSTRAINT FK_CHAMPIONSHIP_STANDING_ON_CHAMPIONSHIP FOREIGN KEY (championship_id) REFERENCES championship (id);

ALTER TABLE club_leaderboard_entry
    ADD CONSTRAINT FK_CLUBLEADERBOARDENTRY_ON_LEADERBOARD FOREIGN KEY (leaderboard_id) REFERENCES club_leaderboard (id);

ALTER TABLE event
    ADD CONSTRAINT FK_EVENT_ON_CHAMPIONSHIP FOREIGN KEY (championship_id) REFERENCES championship (id);

ALTER TABLE event_stages
    ADD CONSTRAINT fk_evesta_on_event FOREIGN KEY (event_id) REFERENCES event (id);

ALTER TABLE event_stages
    ADD CONSTRAINT fk_evesta_on_stage FOREIGN KEY (stages_id) REFERENCES stage (id);