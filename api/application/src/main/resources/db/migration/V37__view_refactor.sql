CREATE TABLE merge_results_view
(
    id   UUID NOT NULL,
    name VARCHAR(255),
    CONSTRAINT pk_mergeresultsview PRIMARY KEY (id)
);

CREATE TABLE merge_results_view_result_views
(
    merge_results_view_id UUID NOT NULL,
    result_views_id       UUID NOT NULL
);

CREATE TABLE partition_element
(
    id                UUID    NOT NULL,
    name              VARCHAR(255),
    partition_view_id UUID,
    custom_order           INTEGER NOT NULL,
    CONSTRAINT pk_partitionelement PRIMARY KEY (id)
);

CREATE TABLE partition_element_racenet_names
(
    partition_element_id UUID NOT NULL,
    racenet_names        VARCHAR(255)
);

CREATE TABLE partition_view
(
    id              UUID NOT NULL,
    results_view_id UUID,
    CONSTRAINT pk_partitionview PRIMARY KEY (id)
);

CREATE TABLE player_filter
(
    id          UUID NOT NULL,
    name        VARCHAR(255),
    filter_type VARCHAR(255),
    CONSTRAINT pk_playerfilter PRIMARY KEY (id)
);

CREATE TABLE player_filter_player_names
(
    player_filter_id UUID NOT NULL,
    player_names     VARCHAR(255)
);

CREATE TABLE single_club_view_power_stage_indices
(
    single_club_view_id UUID NOT NULL,
    power_stage_indices INTEGER
);

CREATE TABLE view_event_restrictions
(
    id                  UUID NOT NULL,
    single_club_view_id UUID,
    challenge_id        VARCHAR(255),
    event_id            VARCHAR(255),
    CONSTRAINT pk_vieweventrestrictions PRIMARY KEY (id)
);

CREATE TABLE view_event_restrictions_vehicles
(
    view_event_restrictions_id UUID NOT NULL,
    vehicles                   VARCHAR(255)
);

ALTER TABLE club_view
    ADD badge_type VARCHAR(255);

ALTER TABLE single_club_view
    ADD name VARCHAR(255);

ALTER TABLE single_club_view
    ADD player_filter_id UUID;

ALTER TABLE merge_results_view_result_views
    ADD CONSTRAINT uc_merge_results_view_result_views_resultviews UNIQUE (result_views_id);

ALTER TABLE merge_results_view
    ADD CONSTRAINT FK_MERGERESULTSVIEW_ON_ID FOREIGN KEY (id) REFERENCES results_view (id);

ALTER TABLE partition_element
    ADD CONSTRAINT FK_PARTITIONELEMENT_ON_PARTITION_VIEW FOREIGN KEY (partition_view_id) REFERENCES partition_view (id);

ALTER TABLE partition_view
    ADD CONSTRAINT FK_PARTITIONVIEW_ON_ID FOREIGN KEY (id) REFERENCES results_view (id);

ALTER TABLE partition_view
    ADD CONSTRAINT FK_PARTITIONVIEW_ON_RESULTS_VIEW FOREIGN KEY (results_view_id) REFERENCES results_view (id);

ALTER TABLE single_club_view
    ADD CONSTRAINT FK_SINGLECLUBVIEW_ON_PLAYERFILTER FOREIGN KEY (player_filter_id) REFERENCES player_filter (id);

ALTER TABLE view_event_restrictions
    ADD CONSTRAINT FK_VIEWEVENTRESTRICTIONS_ON_SINGLE_CLUB_VIEW FOREIGN KEY (single_club_view_id) REFERENCES single_club_view (id);

ALTER TABLE merge_results_view_result_views
    ADD CONSTRAINT fk_merresvieresvie_on_merge_results_view FOREIGN KEY (merge_results_view_id) REFERENCES merge_results_view (id);

ALTER TABLE merge_results_view_result_views
    ADD CONSTRAINT fk_merresvieresvie_on_single_club_view FOREIGN KEY (result_views_id) REFERENCES single_club_view (id);

ALTER TABLE partition_element_racenet_names
    ADD CONSTRAINT fk_partitionelement_racenetnames_on_partition_element FOREIGN KEY (partition_element_id) REFERENCES partition_element (id);

ALTER TABLE player_filter_player_names
    ADD CONSTRAINT fk_playerfilter_playernames_on_player_filter FOREIGN KEY (player_filter_id) REFERENCES player_filter (id);

ALTER TABLE single_club_view_power_stage_indices
    ADD CONSTRAINT fk_singleclubview_powerstageindices_on_single_club_view FOREIGN KEY (single_club_view_id) REFERENCES single_club_view (id);

ALTER TABLE view_event_restrictions_vehicles
    ADD CONSTRAINT fk_vieweventrestrictions_vehicles_on_view_event_restrictions FOREIGN KEY (view_event_restrictions_id) REFERENCES view_event_restrictions (id);

ALTER TABLE player_tiers
DROP
CONSTRAINT fk_platie_on_player;

ALTER TABLE player_tiers
DROP
CONSTRAINT fk_platie_on_tier;

ALTER TABLE single_club_view_players
DROP
CONSTRAINT fk_singleclubview_players_on_single_club_view;

ALTER TABLE tier
DROP
CONSTRAINT fk_tier_on_tier_group;

ALTER TABLE tier_event_restrictions
DROP
CONSTRAINT fk_tieredevent_on_tier;

ALTER TABLE tier_event_restrictions_vehicles
DROP
CONSTRAINT fk_tieredevent_vehicles_on_tiered_event;

ALTER TABLE tiers_view
DROP
CONSTRAINT fk_tiersview_on_id;

DROP TABLE player CASCADE;

DROP TABLE player_tiers CASCADE;

DROP TABLE single_club_view_players CASCADE;

DROP TABLE tier CASCADE;

DROP TABLE tier_event_restrictions CASCADE;

DROP TABLE tier_event_restrictions_vehicles CASCADE;

DROP TABLE tiers_view CASCADE;

ALTER TABLE single_club_view
DROP
COLUMN badge_type;

ALTER TABLE single_club_view
DROP
COLUMN default_powerstage_index;

ALTER TABLE single_club_view
DROP
COLUMN player_restriction;

ALTER TABLE single_club_view
DROP
COLUMN use_power_stage;