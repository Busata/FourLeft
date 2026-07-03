package io.busata.fourleft.backendeasportswrc.application.work.queue;

import io.busata.fourleft.backendeasportswrc.application.timetrial.FetchReport;
import io.busata.fourleft.backendeasportswrc.application.timetrial.TimeTrialFetchService;
import io.busata.fourleft.backendeasportswrc.domain.models.Job;
import io.busata.fourleft.backendeasportswrc.domain.models.JobOutcome;
import io.busata.fourleft.backendeasportswrc.domain.models.JobType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Fetches one time-trial board in full. {@code ref} is the combination id
 * ({@code "{location}-{route}-{surface}-{class}"}); the "fetch all" launcher enqueues one job per
 * existing board so each stays small (well under the stale-job timeout) and they parallelise across
 * the worker pool, all sharing the {@code racenet-timetrial} rate limiter.
 *
 * <p>Ad-hoc only — launched on demand via the {@code devops} CLI, which enqueues the job rows
 * directly (typically after a probe pass has found which boards exist).
 */
@Component
@RequiredArgsConstructor
public class TimeTrialFetchJobHandler implements JobHandler {

    /**
     * Top-N cap for a scheduled/bulk fetch. Bounds a board to {@code ceil(1000/20) = 50} calls no
     * matter how big it is, keeping the whole catalog affordable under the shared rate limit. A board
     * needing its full depth is handled separately (on-demand full fetch, which passes 0 = uncapped).
     */
    private static final int FETCH_CAP = 1000;

    private final TimeTrialFetchService fetchService;
    private final JobService jobService;

    @Override
    public JobType type() {
        return JobType.TT_FETCH;
    }

    @Override
    public JobResult handle(Job job) {
        String combinationId = job.getRef() == null ? "" : job.getRef().trim();
        if (combinationId.isEmpty()) {
            throw new IllegalArgumentException("TT_FETCH ref must be a combination id, got: " + job.getRef());
        }

        // Sequential 20-row pages; heartbeat the lock so the stale-job reclaimer doesn't run this job
        // again in parallel while it's legitimately still going.
        FetchReport report = fetchService.fetchCombination(
                combinationId, FETCH_CAP, () -> jobService.heartbeat(job.getId()));
        // Reuse the club-shaped counters: board fetched -> "leaderboards", stored rows -> "entries".
        // "changed" drives the changed/unchanged signal (new or improved times this fetch).
        return new JobResult(report.changedEntries() > 0, JobOutcome.TT_FETCHED,
                report.boardExists() ? 1 : 0, 0, report.storedEntries());
    }
}
