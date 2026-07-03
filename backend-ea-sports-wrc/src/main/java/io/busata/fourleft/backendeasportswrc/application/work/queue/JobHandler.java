package io.busata.fourleft.backendeasportswrc.application.work.queue;

import io.busata.fourleft.backendeasportswrc.domain.models.Job;
import io.busata.fourleft.backendeasportswrc.domain.models.JobType;
import io.busata.fourleft.backendeasportswrc.infrastructure.time.ApplicationClock;

import java.time.LocalDateTime;

/**
 * Does the actual import work for one job type. Add a new {@code @Component}
 * implementing this and the {@link JobDispatcher} picks it up automatically.
 */
public interface JobHandler {

    JobType type();

    /**
     * Perform the import for a single job. Throwing signals failure and lets the
     * worker retry / fail the job; returning a {@link JobResult} signals success.
     */
    JobResult handle(Job job);

    /**
     * Whether a recurring target with this ref should enqueue a job right now.
     * Lets a handler skip no-op work (e.g. a club with nothing to update) so the
     * queue only ever holds real jobs. Defaults to always enqueue.
     */
    default boolean shouldEnqueue(String ref) {
        return true;
    }

    /**
     * When this target should next run, derived from its own state (e.g. a club's next event
     * boundary / refresh time). Drives the target's {@code nextRunAt} instead of a fixed cadence.
     * Defaults to "now" (run again next tick).
     */
    default LocalDateTime nextRunAt(String ref) {
        return ApplicationClock.now();
    }
}
