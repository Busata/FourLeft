package io.busata.fourleft.backendeasportswrc.application.importer.queue;

import io.busata.fourleft.backendeasportswrc.application.importer.ClubsImporterService;
import io.busata.fourleft.backendeasportswrc.domain.models.ImportJob;
import io.busata.fourleft.backendeasportswrc.domain.models.ImportType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClubImportJobHandler implements JobHandler {

    private final ClubsImporterService clubsImporterService;

    @Override
    public ImportType type() {
        return ImportType.CLUB;
    }

    @Override
    public JobResult handle(ImportJob job) {
        clubsImporterService.runClub(job.getRef());
        // Clubs run on a fixed cadence today; report 'changed' so adaptive backoff
        // (when min != max) keeps them at the floor. Wire real change-detection here
        // later if you want clubs to back off when idle too.
        return JobResult.dataChanged();
    }
}
