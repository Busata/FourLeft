CREATE TABLE club_view
(
    id                   UUID NOT NULL,
    description          VARCHAR(255),
    results_view_id      UUID,
    points_calculator_id UUID,
    CONSTRAINT pk_clubview PRIMARY KEY (id)
);

CREATE TABLE community_challenge_view
(
    id             UUID    NOT NULL,
    post_dailies   BOOLEAN NOT NULL,
    post_weeklies  BOOLEAN NOT NULL,
    post_monthlies BOOLEAN NOT NULL,
    badge_type     VARCHAR(255),
    CONSTRAINT pk_communitychallengeview PRIMARY KEY (id)
);

CREATE TABLE default_points_calculator
(
    id UUID NOT NULL,
    CONSTRAINT pk_defaultpointscalculator PRIMARY KEY (id)
);

CREATE TABLE discord_channel_autopost_configurations
(
    club_view_configuration_id       UUID NOT NULL,
    discord_channel_configuration_id UUID NOT NULL
);

CREATE TABLE discord_channel_configuration
(
    id                         UUID NOT NULL,
    channel_id                 BIGINT,
    club_view_configuration_id UUID,
    CONSTRAINT pk_discordchannelconfiguration PRIMARY KEY (id)
);

CREATE TABLE fixed_points_calculator
(
    id                       UUID    NOT NULL,
    join_championships_count INTEGER NOT NULL,
    offset_championship      UUID,
    point_system_id          UUID,
    CONSTRAINT pk_fixedpointscalculator PRIMARY KEY (id)
);

CREATE TABLE point_system
(
    id          UUID NOT NULL,
    description VARCHAR(255),
    CONSTRAINT pk_pointsystem PRIMARY KEY (id)
);

CREATE TABLE point_system_power_stage_points
(
    point_system_id UUID NOT NULL,
    rank            INTEGER,
    points          INTEGER
);

CREATE TABLE point_system_ranking_points
(
    point_system_id UUID NOT NULL,
    rank            INTEGER,
    points          INTEGER
);

CREATE TABLE points_calculator
(
    id UUID NOT NULL,
    CONSTRAINT pk_pointscalculator PRIMARY KEY (id)
);

CREATE TABLE results_view
(
    id UUID NOT NULL,
    CONSTRAINT pk_resultsview PRIMARY KEY (id)
);

CREATE TABLE single_club_view
(
    id                       UUID    NOT NULL,
    club_id                  BIGINT  NOT NULL,
    use_power_stage          BOOLEAN NOT NULL,
    default_powerstage_index INTEGER NOT NULL,
    badge_type               VARCHAR(255),
    player_restriction       VARCHAR(255),
    CONSTRAINT pk_singleclubview PRIMARY KEY (id)
);

CREATE TABLE single_club_view_players
(
    single_club_view_id UUID NOT NULL,
    players             VARCHAR(255)
);

CREATE TABLE tiers_view
(
    id                       UUID    NOT NULL,
    use_power_stage          BOOLEAN NOT NULL,
    default_powerstage_index INTEGER NOT NULL,
    badge_type               VARCHAR(255),
    CONSTRAINT pk_tiersview PRIMARY KEY (id)
);

ALTER TABLE tier
    DROP CONSTRAINT fk_tier_on_tier_group;

ALTER TABLE club_view
    ADD CONSTRAINT FK_CLUBVIEW_ON_POINTS_CALCULATOR FOREIGN KEY (points_calculator_id) REFERENCES points_calculator (id);

ALTER TABLE club_view
    ADD CONSTRAINT FK_CLUBVIEW_ON_RESULTS_VIEW FOREIGN KEY (results_view_id) REFERENCES results_view (id);

ALTER TABLE community_challenge_view
    ADD CONSTRAINT FK_COMMUNITYCHALLENGEVIEW_ON_ID FOREIGN KEY (id) REFERENCES results_view (id);

ALTER TABLE default_points_calculator
    ADD CONSTRAINT FK_DEFAULTPOINTSCALCULATOR_ON_ID FOREIGN KEY (id) REFERENCES points_calculator (id);

ALTER TABLE discord_channel_configuration
    ADD CONSTRAINT FK_DISCORDCHANNELCONFIGURATION_ON_CLUB_VIEW_CONFIGURATION FOREIGN KEY (club_view_configuration_id) REFERENCES club_view (id);

ALTER TABLE fixed_points_calculator
    ADD CONSTRAINT FK_FIXEDPOINTSCALCULATOR_ON_ID FOREIGN KEY (id) REFERENCES points_calculator (id);

ALTER TABLE fixed_points_calculator
    ADD CONSTRAINT FK_FIXEDPOINTSCALCULATOR_ON_POINTSYSTEM FOREIGN KEY (point_system_id) REFERENCES point_system (id);

ALTER TABLE single_club_view
    ADD CONSTRAINT FK_SINGLECLUBVIEW_ON_ID FOREIGN KEY (id) REFERENCES results_view (id);

ALTER TABLE tiers_view
    ADD CONSTRAINT FK_TIERSVIEW_ON_ID FOREIGN KEY (id) REFERENCES results_view (id);

ALTER TABLE discord_channel_autopost_configurations
    ADD CONSTRAINT fk_dischaautcon_on_club_view FOREIGN KEY (club_view_configuration_id) REFERENCES club_view (id);

ALTER TABLE discord_channel_autopost_configurations
    ADD CONSTRAINT fk_dischaautcon_on_discord_channel_configuration FOREIGN KEY (discord_channel_configuration_id) REFERENCES discord_channel_configuration (id);

ALTER TABLE player_tiers
    ADD CONSTRAINT fk_platie_on_player FOREIGN KEY (player_id) REFERENCES player (id);

ALTER TABLE player_tiers
    ADD CONSTRAINT fk_platie_on_tier FOREIGN KEY (tier_id) REFERENCES tier (id);

ALTER TABLE point_system_power_stage_points
    ADD CONSTRAINT fk_pointsystem_powerstagepoints_on_point_system FOREIGN KEY (point_system_id) REFERENCES point_system (id);

ALTER TABLE point_system_ranking_points
    ADD CONSTRAINT fk_pointsystem_rankingpoints_on_point_system FOREIGN KEY (point_system_id) REFERENCES point_system (id);

ALTER TABLE single_club_view_players
    ADD CONSTRAINT fk_singleclubview_players_on_single_club_view FOREIGN KEY (single_club_view_id) REFERENCES single_club_view (id);
ALTER TABLE point_system
    ADD default_powerstage_point INTEGER;

ALTER TABLE point_system
    ADD default_ranking_point INTEGER;

ALTER TABLE point_system
    ALTER COLUMN default_powerstage_point SET NOT NULL;

ALTER TABLE point_system
    ALTER COLUMN default_ranking_point SET NOT NULL;

ALTER TABLE discord_channel_configuration
    ADD description VARCHAR(255);

DROP TABLE tier_group CASCADE;

insert into results_view values ('7961e20d-6719-43d1-bc9b-00835d6df589');
insert into tiers_view values
    ('7961e20d-6719-43d1-bc9b-00835d6df589',true, -1, 'NONE');

ALTER TABLE tier
    ADD CONSTRAINT FK_TIER_ON_TIER_GROUP FOREIGN KEY (tier_group_id) REFERENCES tiers_view (id);
