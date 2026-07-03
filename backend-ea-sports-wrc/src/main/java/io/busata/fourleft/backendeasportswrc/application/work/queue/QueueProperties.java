package io.busata.fourleft.backendeasportswrc.application.work.queue;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "work-queue")
@Getter
@Setter
public class QueueProperties {

    /** Master switch for the worker system. Off => the legacy {@code ClubUpdateSchedule} importer keeps running. */
    private boolean enabled = false;

    /** Max jobs importing concurrently (across ticks). Caps the virtual-thread drain. */
    private int maxConcurrentJobs = 5;

    /** Max due targets turned into jobs per scheduler tick. */
    private int scheduleBatchSize = 100;

    /**
     * Upper bound on a derived next-due time: a target with no nearer state change is still
     * re-checked after this many seconds (safety net against a missed case). Default 1h.
     */
    private int maxHorizonSeconds = 3600;

    /**
     * After enqueuing a due job, hold its target off for this long so it isn't re-claimed while the
     * import runs; completion then sets the accurate next-due. Default 2min.
     */
    private int inFlightGraceSeconds = 120;

    /** A RUNNING job whose worker vanished (crash) is requeued after this many seconds. */
    private int staleJobSeconds = 600;

    /** How long completed (DONE) jobs are kept before pruning, in hours. */
    private int doneRetentionHours = 24;

    /** How long failed jobs are kept before pruning, in hours (longer, so they're noticed). */
    private int failedRetentionHours = 336;
}
