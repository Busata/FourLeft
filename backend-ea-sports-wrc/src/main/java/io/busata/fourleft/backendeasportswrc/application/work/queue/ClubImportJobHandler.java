package io.busata.fourleft.backendeasportswrc.application.work.queue;

import io.busata.fourleft.backendeasportswrc.application.importer.vt.ClubImportReport;
import io.busata.fourleft.backendeasportswrc.application.importer.vt.ClubImportWorker;
import io.busata.fourleft.backendeasportswrc.domain.models.Job;
import io.busata.fourleft.backendeasportswrc.domain.models.JobType;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import io.busata.fourleft.backendeasportswrc.infrastructure.time.ApplicationClock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Runs a single club import via the virtual-thread worker, and derives the club's next-due time from
 * its domain state (no fixed cadence).
 */
@Component
@RequiredArgsConstructor
public class ClubImportJobHandler implements JobHandler {

    private final ClubImportWorker clubImportWorker;
    private final ClubService clubService;
    private final QueueProperties properties;

    @Override
    public JobType type() {
        return JobType.CLUB;
    }

    @Override
    public boolean shouldEnqueue(String ref) {
        // Only enqueue when an import would actually do something (no no-op jobs).
        return clubService.requiresImport(ref);
    }

    @Override
    public LocalDateTime nextRunAt(String ref) {
        // Earliest moment the club needs work again; fall back to a horizon so a dormant club is
        // still re-checked eventually (safety net against a missed state transition).
        return clubService.nextDueAt(ref)
                .orElseGet(() -> ApplicationClock.now().plusSeconds(properties.getMaxHorizonSeconds()));
    }

    @Override
    public JobResult handle(Job job) {
        // importClub owns its own failure handling (disables sync; does not rethrow), so the job
        // completes even on import error. requeueStale recovers a crashed worker.
        ClubImportReport report = clubImportWorker.importClub(job.getRef());
        return new JobResult(report.changed(), report.outcome(),
                report.leaderboardsUpdated(), report.standingsUpdated(), report.entriesImported());
    }
}
