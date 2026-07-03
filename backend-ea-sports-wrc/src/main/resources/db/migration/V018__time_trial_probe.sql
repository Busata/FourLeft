-- Append-only history of time-trial board observations. Each row records whether the board exists
-- on Racenet, its entry count, and (once the fetch worker runs) how many entries changed since the
-- previous fetch. The latest row per combination is the current state; the series over time feeds
-- popularity / smart scheduling (total = size, changed = churn/activity). Never updated in place.

CREATE SEQUENCE time_trial_probe_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE time_trial_probe
(
    id              BIGINT                   NOT NULL,
    combination_id  VARCHAR(64)              NOT NULL,         -- FK-by-convention to time_trial_combination.id
    board_exists    BOOLEAN                  NOT NULL,         -- did the board exist on Racenet
    total_entries   INTEGER,                                   -- entry count; NULL when the board does not exist
    changed_entries INTEGER,                                   -- entries changed vs previous fetch; NULL for probes (unknown)
    probed_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT pk_time_trial_probe PRIMARY KEY (id)
);

-- Serves "latest probe per combination" (DISTINCT ON combination_id ORDER BY probed_at DESC).
CREATE INDEX idx_time_trial_probe_latest ON time_trial_probe (combination_id, probed_at DESC);
