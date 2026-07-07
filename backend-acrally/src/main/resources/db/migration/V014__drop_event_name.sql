-- Events are no longer named by hand: an event is labelled by the distinct locations of the stages
-- it runs (derived on read), so the stored name is redundant. Drop it.
ALTER TABLE championship_event DROP COLUMN name;
