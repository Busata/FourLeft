CREATE TABLE partition_view_partition_elements
(
    partition_view_id     UUID NOT NULL,
    partition_elements_id UUID NOT NULL
);

CREATE TABLE racenet_filter
(
    id          UUID NOT NULL,
    name        VARCHAR(255),
    filter_mode VARCHAR(255),
    CONSTRAINT pk_racenetfilter PRIMARY KEY (id)
);

CREATE TABLE racenet_filter_racenet_names
(
    racenet_filter_id UUID NOT NULL,
    racenet_names     VARCHAR(255)
);

ALTER TABLE merge_results_view
    ADD racenet_filter_id UUID;

ALTER TABLE single_club_view
    ADD racenet_filter_id UUID;

ALTER TABLE merge_results_view
    ADD CONSTRAINT FK_MERGERESULTSVIEW_ON_RACENETFILTER FOREIGN KEY (racenet_filter_id) REFERENCES racenet_filter (id);

ALTER TABLE single_club_view
    ADD CONSTRAINT FK_SINGLECLUBVIEW_ON_RACENETFILTER FOREIGN KEY (racenet_filter_id) REFERENCES racenet_filter (id);

ALTER TABLE partition_view_partition_elements
    ADD CONSTRAINT fk_parvieparele_on_partition_view FOREIGN KEY (partition_view_id) REFERENCES partition_view (id);

ALTER TABLE partition_view_partition_elements
    ADD CONSTRAINT fk_parvieparele_on_racenet_filter FOREIGN KEY (partition_elements_id) REFERENCES racenet_filter (id);

ALTER TABLE racenet_filter_racenet_names
    ADD CONSTRAINT fk_racenetfilter_racenetnames_on_racenet_filter FOREIGN KEY (racenet_filter_id) REFERENCES racenet_filter (id);

ALTER TABLE merge_results_view
    DROP CONSTRAINT fk_mergeresultsview_on_playerfilter;

ALTER TABLE partition_element
    DROP CONSTRAINT fk_partitionelement_on_partition_view;

ALTER TABLE partition_element_racenet_names
    DROP CONSTRAINT fk_partitionelement_racenetnames_on_partition_element;

ALTER TABLE player_filter_racenet_names
    DROP CONSTRAINT fk_playerfilter_racenetnames_on_player_filter;

ALTER TABLE single_club_view
    DROP CONSTRAINT fk_singleclubview_on_playerfilter;

DROP TABLE partition_element CASCADE;

DROP TABLE partition_element_racenet_names CASCADE;

DROP TABLE player_filter CASCADE;

DROP TABLE player_filter_racenet_names CASCADE;

ALTER TABLE merge_results_view
    DROP COLUMN player_filter_id;

ALTER TABLE single_club_view
    DROP COLUMN player_filter_id;