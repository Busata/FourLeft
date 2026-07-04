package io.busata.fourleft.backendeasportswrc.application.timetrial;

import io.busata.fourleft.backendeasportswrc.domain.models.Job;
import io.busata.fourleft.backendeasportswrc.domain.models.JobType;
import io.busata.fourleft.backendeasportswrc.application.work.queue.JobService;
import io.busata.fourleft.backendeasportswrc.domain.services.queue.JobRepository;
import io.busata.fourleft.backendeasportswrc.domain.services.timetrial.TimeTrialCombinationRepository;
import io.busata.fourleft.backendeasportswrc.domain.services.timetrial.TimeTrialLeaderboardEntryRepository;
import io.busata.fourleft.backendeasportswrc.infrastructure.time.ApplicationClock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * The shared gate for both time-trial sync triggers — the on-demand board "Sync" button and the
 * scheduled refresh sweep. Both ultimately enqueue a {@code TT_FETCH} job for a board via
 * {@link JobService}; this service only decides <em>whether</em> to, applying the per-board freshness
 * window and the in-flight dedupe so a board is never double-fetched. The actual fetch runs in the
 * existing queue worker (see {@code TimeTrialFetchJobHandler}).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TimeTrialSyncService {

    private final TimeTrialCombinationRepository combinationRepository;
    private final TimeTrialLeaderboardEntryRepository entryRepository;
    private final JobRepository jobRepository;
    private final JobService jobService;
    private final TimeTrialSyncProperties properties;

    public enum Status {
        /** A fetch was just enqueued. */
        QUEUED,
        /** A fetch for this board is already queued or running. */
        ALREADY_RUNNING,
        /** The board was fetched too recently; try again after {@code availableAt}. */
        TOO_SOON,
        /** No such combination in the catalog. */
        UNKNOWN_BOARD
    }

    /**
     * @param jobId         the enqueued job's id, when {@link Status#QUEUED}; else null
     * @param lastFetchedAt when the board was last fetched, if ever; else null
     * @param availableAt   the earliest a manual sync is allowed again, when {@link Status#TOO_SOON}; else null
     */
    public record SyncResult(Status status, Long jobId, Instant lastFetchedAt, Instant availableAt) {
    }

    /**
     * Handle a user's "sync this board" request: reject unknown boards, boards with a fetch already in
     * flight, and boards fetched inside the cooldown window; otherwise enqueue a fetch.
     */
    @Transactional
    public SyncResult requestManualSync(String combinationId) {
        if (!combinationRepository.existsById(combinationId)) {
            return new SyncResult(Status.UNKNOWN_BOARD, null, null, null);
        }
        if (jobRepository.existsActive(JobType.TT_FETCH.name(), combinationId)) {
            return new SyncResult(Status.ALREADY_RUNNING, null, lastFetchedAt(combinationId).orElse(null), null);
        }

        Optional<Instant> lastFetchedAt = lastFetchedAt(combinationId);
        Instant cutoff = now().minus(Duration.ofHours(properties.getManualCooldownHours()));
        if (lastFetchedAt.isPresent() && lastFetchedAt.get().isAfter(cutoff)) {
            Instant availableAt = lastFetchedAt.get().plus(Duration.ofHours(properties.getManualCooldownHours()));
            return new SyncResult(Status.TOO_SOON, null, lastFetchedAt.get(), availableAt);
        }

        Job job = jobService.enqueue(JobType.TT_FETCH, combinationId);
        log.info("Queued user-requested TT_FETCH for board {} (job {})", combinationId, job.getId());
        return new SyncResult(Status.QUEUED, job.getId(), lastFetchedAt.orElse(null), null);
    }

    /**
     * Enqueue a fetch for every board with stored entries whose last fetch is older than the refresh
     * interval and which has no fetch already in flight. Bounded by {@code sweepBatchLimit} per call.
     *
     * @return number of fetch jobs enqueued
     */
    @Transactional
    public int enqueueScheduledRefresh() {
        Instant cutoff = now().minus(Duration.ofHours(properties.getRefreshIntervalHours()));
        int enqueued = 0;
        for (Object[] row : entryRepository.findLatestFetchedAtPerCombination()) {
            if (enqueued >= properties.getSweepBatchLimit()) {
                log.warn("TT refresh sweep hit batch limit ({}); remaining due boards run next tick",
                        properties.getSweepBatchLimit());
                break;
            }
            String combinationId = (String) row[0];
            Instant fetchedAt = (Instant) row[1];
            if (fetchedAt != null && fetchedAt.isAfter(cutoff)) {
                continue; // still fresh
            }
            if (jobRepository.existsActive(JobType.TT_FETCH.name(), combinationId)) {
                continue; // already queued/running
            }
            jobService.enqueue(JobType.TT_FETCH, combinationId);
            enqueued++;
        }
        return enqueued;
    }

    private Optional<Instant> lastFetchedAt(String combinationId) {
        return entryRepository.findLatestFetchedAt(combinationId);
    }

    /** Instant "now" off the same thread-local clock {@link ApplicationClock} uses, so tests can pin it. */
    private static Instant now() {
        return Instant.now(ApplicationClock.CLOCK.get());
    }
}
