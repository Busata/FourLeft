package io.busata.fourleft.backendeasportswrc.application.importer.process.handlers;

import io.busata.fourleft.backendeasportswrc.application.importer.importers.ClubDetailsImporter;
import io.busata.fourleft.backendeasportswrc.application.importer.process.ClubImportProcess;
import io.busata.fourleft.backendeasportswrc.application.importer.process.ClubImportProcessHandler;
import io.busata.fourleft.backendeasportswrc.application.importer.process.ProcessState;
import io.busata.fourleft.backendeasportswrc.application.importer.results.ClubDetailsUpdatedResult;
import io.busata.fourleft.backendeasportswrc.application.importer.results.FailedClubUpdateResult;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateClubProcessHandler implements ClubImportProcessHandler {

    private final ClubService clubService;
    private final ClubDetailsImporter clubDetailsImporter;

    @Getter
    private final Map<ProcessState, Consumer<ClubImportProcess>> strategies = Map.of(
            ProcessState.UPDATE_EXISTING_CLUB, this::processUpdateExistingClub,
            ProcessState.FETCHING_CLUB_DETAILS, this::processFetchClubDetails,
            ProcessState.FETCHING_CLUB_DETAILS_FINISHED, this::processFetchClubDetailsFinished
    );

    private void processUpdateExistingClub(ClubImportProcess process) {
        boolean activeEventFinished = clubService.hasActiveEventThatFinished(process.getClubId());

        if (activeEventFinished) {
            process.setState(ProcessState.UPDATE_EVENT_ENDED);
        } else {
            process.setDetails(clubDetailsImporter.fetchDetails(process.getClubId()));
            process.setState(ProcessState.FETCHING_CLUB_DETAILS);
        }
    }

    private void processFetchClubDetails(ClubImportProcess process) {
        if (process.getDetails().isDone()) {
            process.setState(ProcessState.FETCHING_CLUB_DETAILS_FINISHED);
        }
    }

    private void processFetchClubDetailsFinished(ClubImportProcess process) {
        var result = process.getDetails().join();

        if (result instanceof ClubDetailsUpdatedResult updatedResult) {
            this.clubService.updateClub(updatedResult.getClubDetails(), updatedResult.getChampionships());
            process.markDone();
        }
        if (result instanceof FailedClubUpdateResult) {
            log.error("IMPORTER - Club {} failed to update its details, skipping", process.getClubId());
            process.markFailed();
        }

    }
}
