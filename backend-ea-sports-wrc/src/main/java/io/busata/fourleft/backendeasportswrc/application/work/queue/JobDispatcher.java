package io.busata.fourleft.backendeasportswrc.application.work.queue;

import io.busata.fourleft.backendeasportswrc.domain.models.Job;
import io.busata.fourleft.backendeasportswrc.domain.models.JobType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Routes a job to the {@link JobHandler} registered for its type. */
@Component
public class JobDispatcher {

    private final Map<JobType, JobHandler> handlers;

    public JobDispatcher(List<JobHandler> handlers) {
        this.handlers = handlers.stream()
                .collect(Collectors.toMap(JobHandler::type, Function.identity()));
    }

    public JobResult dispatch(Job job) {
        JobHandler handler = handlers.get(job.getType());
        if (handler == null) {
            throw new IllegalStateException("No JobHandler registered for type " + job.getType());
        }
        return handler.handle(job);
    }

    /** Whether a target of this type/ref has real work to do right now. */
    public boolean shouldEnqueue(JobType type, String ref) {
        JobHandler handler = handlers.get(type);
        return handler != null && handler.shouldEnqueue(ref);
    }
}
