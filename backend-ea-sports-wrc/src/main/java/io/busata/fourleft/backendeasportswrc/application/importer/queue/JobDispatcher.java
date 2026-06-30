package io.busata.fourleft.backendeasportswrc.application.importer.queue;

import io.busata.fourleft.backendeasportswrc.domain.models.ImportJob;
import io.busata.fourleft.backendeasportswrc.domain.models.ImportType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Routes a job to the {@link JobHandler} registered for its type. */
@Component
public class JobDispatcher {

    private final Map<ImportType, JobHandler> handlers;

    public JobDispatcher(List<JobHandler> handlers) {
        this.handlers = handlers.stream()
                .collect(Collectors.toMap(JobHandler::type, Function.identity()));
    }

    public JobResult dispatch(ImportJob job) {
        JobHandler handler = handlers.get(job.getType());
        if (handler == null) {
            throw new IllegalStateException("No JobHandler registered for type " + job.getType());
        }
        return handler.handle(job);
    }
}
