package io.busata.fourleft.backendeasportswrc.application.work.queue;

import io.busata.fourleft.backendeasportswrc.domain.models.Job;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Drives the worker system with three tiny scheduled loops:
 * <ol>
 *   <li>{@link #scheduleTick()} — keep the target list in sync and turn due targets into jobs.</li>
 *   <li>{@link #workTick()} — drain pending jobs, up to {@code maxConcurrentJobs} at a time.</li>
 *   <li>{@link #pruneTick()} — bound the job table.</li>
 * </ol>
 * Jobs run on virtual threads (imports are blocking I/O) with a {@link Semaphore} capping concurrency
 * — the DB-backed equivalent of the in-memory {@code inFlight}/{@code maxConcurrentClubs} top-up.
 * Claims use SKIP LOCKED so it's safe across instances. The bean only activates when
 * {@code work-queue.enabled=true}; otherwise {@code ClubUpdateSchedule} remains the only importer.
 */
@Component
@Profile("!test")
@ConditionalOnProperty(prefix = "work-queue", name = "enabled", havingValue = "true")
@Slf4j
public class QueueWorker {

    private final JobTargetService targetService;
    private final JobService jobService;
    private final JobDispatcher dispatcher;

    private final ExecutorService workExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private final Semaphore slots;

    public QueueWorker(JobTargetService targetService, JobService jobService,
                       JobDispatcher dispatcher, QueueProperties properties) {
        this.targetService = targetService;
        this.jobService = jobService;
        this.dispatcher = dispatcher;
        this.slots = new Semaphore(properties.getMaxConcurrentJobs());
    }

    @Scheduled(fixedDelayString = "${work-queue.schedule-tick-ms:5000}")
    public void scheduleTick() {
        try {
            targetService.syncClubTargets();
            int enqueued = targetService.enqueueDueTargets();
            if (enqueued > 0) {
                log.debug("Enqueued {} job(s) from due targets", enqueued);
            }
        } catch (Exception ex) {
            log.error("Schedule tick failed", ex);
        }
    }

    @Scheduled(fixedDelayString = "${work-queue.prune-tick-ms:3600000}")
    public void pruneTick() {
        try {
            jobService.prune();
        } catch (Exception ex) {
            log.error("Prune tick failed", ex);
        }
    }

    /**
     * Top the in-flight set up to {@code maxConcurrentJobs}: acquire a permit, claim a job, and run
     * it on a virtual thread that releases the permit when done. Stops when at capacity or drained.
     */
    @Scheduled(fixedDelayString = "${work-queue.work-tick-ms:1000}")
    public void workTick() {
        jobService.requeueStale();

        while (slots.tryAcquire()) {
            Optional<Job> claimed;
            try {
                claimed = jobService.claimNext();
            } catch (RuntimeException ex) {
                slots.release();
                throw ex;
            }
            if (claimed.isEmpty()) {
                slots.release();
                return; // queue drained for now
            }
            Job job = claimed.get();
            workExecutor.submit(() -> {
                try {
                    process(job);
                } finally {
                    slots.release();
                }
            });
        }
    }

    private void process(Job job) {
        try {
            dispatcher.dispatch(job);
            jobService.complete(job);
            if (job.getTargetId() != null) {
                // Recompute next-due from the post-import state (timestamps/boundaries just advanced).
                targetService.rescheduleFromState(job.getTargetId());
            }
        } catch (Exception ex) {
            jobService.fail(job, ex);
        }
    }

    @PreDestroy
    void shutdown() {
        workExecutor.shutdown();
    }
}
