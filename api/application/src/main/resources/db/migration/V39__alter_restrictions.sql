ALTER TABLE view_event_restrictions
    ADD results_view_id UUID;

ALTER TABLE view_event_restrictions
    ADD CONSTRAINT FK_VIEWEVENTRESTRICTIONS_ON_RESULTS_VIEW FOREIGN KEY (results_view_id) REFERENCES results_view (id);

ALTER TABLE view_event_restrictions
DROP
CONSTRAINT fk_vieweventrestrictions_on_single_club_view;

ALTER TABLE view_event_restrictions
DROP
COLUMN single_club_view_id;