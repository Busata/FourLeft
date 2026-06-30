package io.busata.fourleft.backendeasportswrc.application.work.queue;

import io.busata.fourleft.backendeasportswrc.application.importer.ClubsImporterService;
import io.busata.fourleft.backendeasportswrc.domain.models.Job;
import io.busata.fourleft.backendeasportswrc.domain.models.JobType;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClubImportJobHandler implements JobHandler {

    private final ClubsImporterService clubsImporterService;
    private final ClubService clubService;

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
    public JobResult handle(Job job) {
        clubsImporterService.runClub(job.getRef());
        // Clubs run on a fixed cadence today; report 'changed' so adaptive backoff
        // (when min != max) keeps them at the floor. Wire real change-detection here
        // later if you want clubs to back off when idle too.
        return JobResult.dataChanged();
    }
}
