-- A DNF spends the driver's one shot at a stage, which is the point — but a DNF caused by a
-- technical mishap (crashed game, agent died, an arm left waiting by accident) shouldn't. Club
-- owners can now hand the shot back: the arm keeps its DNF outcome for the record and gains a
-- revert stamp, and every "has this driver already used this stage up?" check ignores reverted
-- arms. Keeping the row (rather than deleting it) leaves an audit trail of who granted the retry.
ALTER TABLE event_arm
    ADD COLUMN reverted_at TIMESTAMP WITHOUT TIME ZONE,
    ADD COLUMN reverted_by UUID,
    ADD CONSTRAINT fk_event_arm_reverted_by FOREIGN KEY (reverted_by) REFERENCES app_user (id);

-- The owner's DNF panel lists a whole event's DNFs; the one-shot checks read them per driver.
CREATE INDEX ix_event_arm_event_outcome ON event_arm (event_id, outcome);
