package io.busata.fourleft.backendeasportswrc.application.work.queue;

import io.busata.fourleft.backendeasportswrc.domain.models.Job;
import io.busata.fourleft.backendeasportswrc.domain.models.JobOutcome;
import io.busata.fourleft.backendeasportswrc.domain.models.JobType;
import io.busata.fourleft.backendeasportswrc.domain.services.clubExport.ClubExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Regenerates one club's cached results export (ref = clubId). Enqueued in a fan-out by
 * {@code ClubExportService.enqueueScheduledExport} so each club's rebuild is its own small job on the
 * virtual-thread pool, with retry/visibility on the queue — instead of one long synchronous sweep.
 */
@Component
@RequiredArgsConstructor
public class ClubExportJobHandler implements JobHandler {

    private final ClubExportService clubExportService;

    @Override
    public JobType type() {
        return JobType.CLUB_EXPORT;
    }

    @Override
    public JobResult handle(Job job) {
        clubExportService.exportClub(job.getRef());
        return new JobResult(true, JobOutcome.CLUB_EXPORTED, 0, 0, 0);
    }
}
