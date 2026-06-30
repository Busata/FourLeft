package io.busata.fourleft.backendeasportswrc.application.importer.queue;

import io.busata.fourleft.backendeasportswrc.domain.models.ImportJob;
import io.busata.fourleft.backendeasportswrc.domain.models.ImportType;

/**
 * Does the actual import work for one job type. Add a new {@code @Component}
 * implementing this and the {@link JobDispatcher} picks it up automatically.
 */
public interface JobHandler {

    ImportType type();

    /**
     * Perform the import for a single job. Throwing signals failure and lets the
     * worker retry / fail the job; returning a {@link JobResult} signals success.
     */
    JobResult handle(ImportJob job);

    /**
     * Whether a recurring target with this ref should enqueue a job right now.
     * Lets a handler skip no-op work (e.g. a club with nothing to update) so the
     * queue only ever holds real jobs. Defaults to always enqueue.
     */
    default boolean shouldEnqueue(String ref) {
        return true;
    }
}
