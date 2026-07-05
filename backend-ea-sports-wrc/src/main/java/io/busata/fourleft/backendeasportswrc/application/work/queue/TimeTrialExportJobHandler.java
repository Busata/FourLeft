package io.busata.fourleft.backendeasportswrc.application.work.queue;

import io.busata.fourleft.backendeasportswrc.domain.models.Job;
import io.busata.fourleft.backendeasportswrc.domain.models.JobOutcome;
import io.busata.fourleft.backendeasportswrc.domain.models.JobType;
import io.busata.fourleft.backendeasportswrc.domain.services.timetrial.TimeTrialExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Regenerates one time-trial board's CSV export (ref = combinationId). Enqueued by
 * {@code TimeTrialExportTrigger} whenever a board's fetch lands (via the RabbitMQ
 * {@code TT_BOARD_FETCHED} event), and manually per board via the devops CLI.
 */
@Component
@RequiredArgsConstructor
public class TimeTrialExportJobHandler implements JobHandler {

    private final TimeTrialExportService exportService;

    @Override
    public JobType type() {
        return JobType.TT_EXPORT;
    }

    @Override
    public JobResult handle(Job job) {
        int entries = exportService.exportBoard(job.getRef());
        // Same counter shape as TT_FETCHED: board exported -> "leaderboards", rows written -> "entries".
        return new JobResult(true, JobOutcome.TT_EXPORTED, 1, 0, entries);
    }
}
