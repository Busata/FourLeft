package io.busata.fourleft.backendeasportswrc.application.work.queue;

import io.busata.fourleft.backendeasportswrc.application.timetrial.ProbeReport;
import io.busata.fourleft.backendeasportswrc.application.timetrial.TimeTrialProbeService;
import io.busata.fourleft.backendeasportswrc.domain.models.Job;
import io.busata.fourleft.backendeasportswrc.domain.models.JobOutcome;
import io.busata.fourleft.backendeasportswrc.domain.models.JobType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Probes every time-trial board for one rally. {@code ref} is the rally's {@code locationId}; the
 * "probe all" launcher enqueues one job per rally so each stays small (well under the stale-job
 * timeout) and they parallelise across the worker pool.
 *
 * <p>Ad-hoc only — there's no recurring {@code JobTarget} for probes; they're launched on demand
 * (see the {@code devops} CLI, which enqueues the job rows directly).
 */
@Component
@RequiredArgsConstructor
public class TimeTrialProbeJobHandler implements JobHandler {

    private final TimeTrialProbeService probeService;

    @Override
    public JobType type() {
        return JobType.TT_PROBE;
    }

    @Override
    public JobResult handle(Job job) {
        long locationId;
        try {
            locationId = Long.parseLong(job.getRef().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("TT_PROBE ref must be a numeric locationId, got: " + job.getRef());
        }

        ProbeReport report = probeService.probeLocation(locationId);
        // Reuse the club-shaped counters: boards found -> "leaderboards", entry total -> "entries".
        return new JobResult(true, JobOutcome.TT_PROBED, report.boardsFound(), 0, report.totalEntries());
    }
}
