-- The server-side "I'm about to drive this stage" record that powers the agent's Start button.
-- A driver arms a specific variant (stage) of an event; the arm binds to the NEXT agent_session
-- they open (a session opened before the arm existed can never bind, so pressing Start mid-run —
-- or right before a finish — cannot capture that run). When the run's result arrives the arm is
-- consumed with an outcome; an aborted/restarted run unbinds it so the next fresh run re-binds.
CREATE TABLE event_arm
(
    id         UUID                        NOT NULL,
    user_id    UUID                        NOT NULL,
    event_id   UUID                        NOT NULL,
    variant_id UUID                        NOT NULL,
    -- ARMED (waiting for a run to open), BOUND (attached to an open session), CONSUMED (a run
    -- finished — see outcome), CANCELLED (disarmed or superseded by a new arm).
    status     VARCHAR(20)                 NOT NULL,
    -- The agent_session the arm bound to, and the stage_result that consumed it (when recorded).
    session_id UUID,
    result_id  UUID,
    -- Why a consumed arm did or didn't score: RECORDED, SLOWER (kept an existing better time),
    -- WRONG_STAGE, WRONG_CAR, EVENT_CLOSED.
    outcome    VARCHAR(20),
    armed_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_event_arm PRIMARY KEY (id),
    CONSTRAINT fk_event_arm_user FOREIGN KEY (user_id) REFERENCES app_user (id),
    CONSTRAINT fk_event_arm_event FOREIGN KEY (event_id) REFERENCES championship_event (id) ON DELETE CASCADE,
    CONSTRAINT fk_event_arm_variant FOREIGN KEY (variant_id) REFERENCES variant (id),
    CONSTRAINT fk_event_arm_session FOREIGN KEY (session_id) REFERENCES agent_session (id),
    CONSTRAINT fk_event_arm_result FOREIGN KEY (result_id) REFERENCES stage_result (id)
);
-- A user has at most one live arm (armed or bound) at a time; consuming/cancelling frees the slot.
CREATE UNIQUE INDEX ux_event_arm_live ON event_arm (user_id) WHERE status IN ('ARMED', 'BOUND');
CREATE INDEX ix_event_arm_session ON event_arm (session_id);
CREATE INDEX ix_event_arm_user ON event_arm (user_id, created_at DESC);
