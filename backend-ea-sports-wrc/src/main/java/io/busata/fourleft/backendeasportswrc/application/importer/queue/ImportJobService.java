package io.busata.fourleft.backendeasportswrc.application.importer.queue;

import io.busata.fourleft.backendeasportswrc.domain.models.ImportJob;
import io.busata.fourleft.backendeasportswrc.domain.models.ImportJobStatus;
import io.busata.fourleft.backendeasportswrc.domain.models.ImportType;
import io.busata.fourleft.backendeasportswrc.domain.services.queue.ImportJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/** Owns the {@code import_job} lifecycle: enqueue, claim, complete, fail, recover. */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImportJobService {

    private final ImportJobRepository jobRepository;
    private final ImportQueueProperties properties;

    /** Enqueue an ad-hoc one-off job (e.g. "import club X right now"). */
    @Transactional
    public ImportJob enqueue(ImportType type, String ref) {
        return enqueue(type, ref, null);
    }

    /**
     * Enqueue a job for a recurring target, unless one is already in flight for it
     * (prevents pile-up when an import takes longer than its cadence).
     *
     * @return the new job, or empty if a job for this target was already active
     */
    @Transactional
    public Optional<ImportJob> enqueueForTarget(ImportType type, String ref, Long targetId) {
        if (jobRepository.hasActiveJobForTarget(targetId)) {
            return Optional.empty();
        }
        return Optional.of(enqueue(type, ref, targetId));
    }

    private ImportJob enqueue(ImportType type, String ref, Long targetId) {
        return jobRepository.save(new ImportJob(type, ref, targetId));
    }

    /**
     * Atomically claim the next runnable job and mark it RUNNING. The row lock taken
     * by the SKIP LOCKED select is held until this transaction commits, so no other
     * worker can claim the same job in the window before it flips to RUNNING.
     */
    @Transactional
    public Optional<ImportJob> claimNext() {
        return jobRepository.claimNext().map(job -> {
            job.markRunning();
            return job;
        });
    }

    @Transactional
    public void complete(ImportJob job) {
        job.markDone();
        jobRepository.save(job);
    }

    /** Retry with linear backoff while attempts remain, otherwise mark FAILED. */
    @Transactional
    public void fail(ImportJob job, Exception ex) {
        job.setLastError(ex.getMessage());
        if (job.getAttempts() < properties.getMaxAttempts()) {
            job.setStatus(ImportJobStatus.PENDING);
            job.setLockedAt(null);
            job.setRunAfter(Instant.now().plusSeconds((long) job.getAttempts() * properties.getRetryBackoffSeconds()));
            log.warn("Job {} ({} {}) failed attempt {}, will retry: {}",
                    job.getId(), job.getType(), job.getRef(), job.getAttempts(), ex.getMessage());
        } else {
            job.setStatus(ImportJobStatus.FAILED);
            job.setLockedAt(null);
            log.error("Job {} ({} {}) failed permanently after {} attempts",
                    job.getId(), job.getType(), job.getRef(), job.getAttempts(), ex);
        }
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
}
