-- Current fetched rows of every time-trial board. Unlike time_trial_probe (append-only history),
-- this holds only the latest snapshot: a fetch of a board deletes its rows and re-inserts the page
-- it just pulled. combination_id is an FK-by-convention to time_trial_combination.id. Times are the
-- raw Racenet-formatted strings ("mm:ss.SSS"), stored verbatim.

CREATE TABLE time_trial_entry
(
    id                  UUID                     NOT NULL,
    combination_id      VARCHAR(64)              NOT NULL,       -- FK-by-convention to time_trial_combination.id
    ssid                VARCHAR(64),
    display_name        VARCHAR(255),
    wrc_player_id       VARCHAR(64),
    rank                BIGINT,
    nationality_id      BIGINT,
    platform            BIGINT,
    vehicle             VARCHAR(255),
    time                VARCHAR(32),                             -- raw "hh:mm:ss.fffffff" from Racenet
    difference_to_first VARCHAR(32),
    time_penalty        VARCHAR(32),
    splits              JSONB,                                   -- per-sector split times, as a json array of strings
    fetched_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT pk_time_trial_entry PRIMARY KEY (id)
);

-- Serves load / delete of a whole board when re-fetching it.
CREATE INDEX idx_time_trial_entry_combination ON time_trial_entry (combination_id);
