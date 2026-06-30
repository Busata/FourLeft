package io.busata.fourleft.backendeasportswrc.application.importer.queue;

import io.busata.fourleft.backendeasportswrc.domain.models.ImportJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Drives the worker system with two tiny scheduled loops:
 * <ol>
 *   <li>{@link #scheduleTick()} — keep the target list in sync and turn due targets into jobs.</li>
 *   <li>{@link #workTick()} — drain pending jobs.</li>
 * </ol>
 * Both are safe to run on multiple instances (claims use SKIP LOCKED). The whole
 * bean only activates when {@code import-queue.enabled=true}; otherwise the legacy
 * {@code ClubUpdateSchedule} importer remains the only thing running.
 */
@Component
@Profile("!test")
@ConditionalOnProperty(prefix = "import-queue", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class ImportQueueWorker {

    private final ImportTargetService targetService;
    private final ImportJobService jobService;
    private final JobDispatcher dispatcher;
    private final ImportQueueProperties properties;

    @Scheduled(fixedDelayString = "${import-queue.schedule-tick-ms:5000}")
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

    @Scheduled(fixedDelayString = "${import-queue.work-tick-ms:1000}")
    public void workTick() {
        jobService.requeueStale();

        for (int i = 0; i < properties.getBatchSize(); i++) {
            Optional<ImportJob> claimed = jobService.claimNext();
            if (claimed.isEmpty()) {
                return; // queue drained for now
            }
            process(claimed.get());
        }
    }

    private void process(ImportJob job) {
        try {
            JobResult result = dispatcher.dispatch(job);
            jobService.complete(job);
            if (job.getTargetId() != null) {
                targetService.applyAdaptiveCadence(job.getTargetId(), result.changed());
            }
        } catch (Exception ex) {
            jobService.fail(job, ex);
        }
    }
}
