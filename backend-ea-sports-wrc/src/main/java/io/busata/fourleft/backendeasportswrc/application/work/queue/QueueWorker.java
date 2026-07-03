package io.busata.fourleft.backendeasportswrc.application.work.queue;

import io.busata.fourleft.backendeasportswrc.domain.models.Job;
import io.busata.fourleft.backendeasportswrc.domain.models.JobType;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
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
 * Jobs run on virtual threads (imports are blocking I/O) with a {@link Semaphore} <em>per job type</em>
 * capping concurrency, so one type flooding the queue (e.g. a full time-trial sweep) can't starve
 * another (club syncs keep their own slots). Claims use SKIP LOCKED so it's safe across instances.
 * The bean only activates when {@code work-queue.enabled=true}; otherwise {@code ClubUpdateSchedule}
 * remains the only importer.
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
    /** One semaphore per job type — each type gets its own {@code maxConcurrentJobs} budget. */
    private final Map<JobType, Semaphore> slotsByType = new EnumMap<>(JobType.class);

    public QueueWorker(JobTargetService targetService, JobService jobService,
                       JobDispatcher dispatcher, QueueProperties properties) {
        this.targetService = targetService;
        this.jobService = jobService;
        this.dispatcher = dispatcher;
        for (JobType type : JobType.values()) {
            slotsByType.put(type, new Semaphore(properties.getMaxConcurrentJobs()));
        }
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
     * Top each job type's in-flight set up to its {@code maxConcurrentJobs} budget. Draining per type
     * keeps the types independent — a backlog of one can't consume another's slots. A claim failure on
     * one type is logged and doesn't block the others this tick.
     */
    @Scheduled(fixedDelayString = "${work-queue.work-tick-ms:1000}")
    public void workTick() {
        jobService.requeueStale();

        for (JobType type : JobType.values()) {
            try {
                drainType(type);
            } catch (Exception ex) {
                log.error("Work tick failed for {}", type, ex);
            }
        }
    }

    /** Fill one type's free slots: acquire a permit, claim a job of that type, run it, release on done. */
    private void drainType(JobType type) {
        Semaphore slots = slotsByType.get(type);
        while (slots.tryAcquire()) {
            Optional<Job> claimed;
            try {
                claimed = jobService.claimNext(type);
            } catch (RuntimeException ex) {
                slots.release();
                throw ex;
            }
            if (claimed.isEmpty()) {
                slots.release();
                return; // this type drained for now
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
            JobResult result = dispatcher.dispatch(job);
            jobService.complete(job, result);
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
