CREATE TABLE concatenation_view
(
    id   UUID NOT NULL,
    name VARCHAR(255),
    CONSTRAINT pk_concatenationview PRIMARY KEY (id)
);

CREATE TABLE concatenation_view_result_views
(
    concatenation_view_id UUID NOT NULL,
    result_views_id       UUID NOT NULL
);

ALTER TABLE merge_results_view
    ADD player_filter_id UUID;

ALTER TABLE concatenation_view_result_views
    ADD CONSTRAINT uc_concatenation_view_result_views_resultviews UNIQUE (result_views_id);

ALTER TABLE concatenation_view
    ADD CONSTRAINT FK_CONCATENATIONVIEW_ON_ID FOREIGN KEY (id) REFERENCES results_view (id);

ALTER TABLE merge_results_view
    ADD CONSTRAINT FK_MERGERESULTSVIEW_ON_PLAYERFILTER FOREIGN KEY (player_filter_id) REFERENCES player_filter (id);

ALTER TABLE concatenation_view_result_views
    ADD CONSTRAINT fk_convieresvie_on_concatenation_view FOREIGN KEY (concatenation_view_id) REFERENCES concatenation_view (id);

ALTER TABLE concatenation_view_result_views
    ADD CONSTRAINT fk_convieresvie_on_single_club_view FOREIGN KEY (result_views_id) REFERENCES single_club_view (id);