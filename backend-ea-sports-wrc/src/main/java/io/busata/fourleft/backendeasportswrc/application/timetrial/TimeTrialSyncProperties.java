package io.busata.fourleft.backendeasportswrc.application.timetrial;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Tunables for the two time-trial sync triggers (the board "Sync" button and the scheduled refresh). */
@Component
@ConfigurationProperties(prefix = "time-trial-sync")
@Getter
@Setter
public class TimeTrialSyncProperties {

    /** Minimum age of a board's last fetch before a user may request another sync of it. */
    private int manualCooldownHours = 3;

    /** How stale a board's last fetch must be before the scheduled sweep refreshes it. */
    private int refreshIntervalHours = 48;

    /** Master switch for the scheduled refresh sweep. */
    private boolean sweepEnabled = true;

    /**
     * Safety cap on jobs enqueued per sweep tick. The natural spread (hourly sweep, 48h threshold) keeps
     * each tick small; this only bounds a pathological catch-up (e.g. first run, or after a long outage).
     */
    private int sweepBatchLimit = 500;
}
