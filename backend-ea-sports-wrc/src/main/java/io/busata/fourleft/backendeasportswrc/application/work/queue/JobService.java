package io.busata.fourleft.backendeasportswrc.application.work.queue;

import io.busata.fourleft.backendeasportswrc.domain.models.Job;
import io.busata.fourleft.backendeasportswrc.domain.models.JobStatus;
import io.busata.fourleft.backendeasportswrc.domain.models.JobType;
import io.busata.fourleft.backendeasportswrc.domain.services.queue.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** Owns the {@code job} lifecycle: enqueue, claim, complete, fail, recover. */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobService {

    private final JobRepository jobRepository;
    private final QueueProperties properties;

    /** Enqueue an ad-hoc one-off job (e.g. "import club X right now"). */
    @Transactional
    public Job enqueue(JobType type, String ref) {
        return enqueue(type, ref, null);
    }

    /**
     * Enqueue a job for a recurring target, unless one is already in flight for it
     * (prevents pile-up when an import takes longer than its cadence).
     *
     * @return the new job, or empty if a job for this target was already active
     */
    @Transactional
    public Optional<Job> enqueueForTarget(JobType type, String ref, Long targetId) {
        if (jobRepository.hasActiveJobForTarget(targetId)) {
            return Optional.empty();
        }
        return Optional.of(enqueue(type, ref, targetId));
    }

    private Job enqueue(JobType type, String ref, Long targetId) {
        return jobRepository.save(new Job(type, ref, targetId));
    }

    /**
     * Atomically claim the next runnable job and mark it RUNNING. The row lock taken
     * by the SKIP LOCKED select is held until this transaction commits, so no other
     * worker can claim the same job in the window before it flips to RUNNING.
     */
    @Transactional
    public Optional<Job> claimNext() {
        return jobRepository.claimNext().map(job -> {
            job.markRunning();
            return job;
        });
    }

    /** Mark DONE and record what the run did (outcome, item counts, duration via finishedAt). */
    @Transactional
    public void complete(Job job, JobResult result) {
        job.recordOutcome(result.outcome(), result.changed(),
                result.leaderboardsUpdated(), result.standingsUpdated(), result.entriesImported());
        job.markDone();
        jobRepository.save(job);
    }

    /** Mark FAILED and record the error. No retry — the target's schedule brings the work back. */
    @Transactional
    public void fail(Job job, Exception ex) {
        job.setLastError(ex.getMessage());
        job.markFailed();
        log.error("Job {} ({} {}) failed", job.getId(), job.getType(), job.getRef(), ex);
        jobRepository.save(job);
    }

    /** Recover jobs orphaned by a crashed worker (RUNNING past the stale threshold). */
    @Transactional
    public int requeueStale() {
        int requeued = jobRepository.requeueStale(properties.getStaleJobSeconds());
        if (requeued > 0) {
            log.warn("Requeued {} stale RUNNING job(s)", requeued);
        }
        return requeued;
    }

    /** Delete terminal jobs past their retention window so the table stays bounded. */
    @Transactional
    public void prune() {
        int done = jobRepository.deleteByStatusOlderThanHours(
                JobStatus.DONE.name(), properties.getDoneRetentionHours());
        int failed = jobRepository.deleteByStatusOlderThanHours(
                JobStatus.FAILED.name(), properties.getFailedRetentionHours());
        if (done > 0 || failed > 0) {
            log.info("Pruned {} done and {} failed job(s)", done, failed);
        }
    }
}
