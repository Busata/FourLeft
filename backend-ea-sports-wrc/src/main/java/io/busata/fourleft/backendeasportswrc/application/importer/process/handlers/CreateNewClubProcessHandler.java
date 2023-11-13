package io.busata.fourleft.backendeasportswrc.application.importer.process.handlers;

import io.busata.fourleft.backendeasportswrc.application.importer.importers.ClubDetailsImporter;
import io.busata.fourleft.backendeasportswrc.application.importer.process.ClubImportProcess;
import io.busata.fourleft.backendeasportswrc.application.importer.process.ClubImportProcessHandler;
import io.busata.fourleft.backendeasportswrc.application.importer.process.ProcessState;
import io.busata.fourleft.backendeasportswrc.application.importer.results.ClubCreationResult;
import io.busata.fourleft.backendeasportswrc.application.importer.results.FailedClubCreationResult;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import io.busata.fourleft.backendeasportswrc.domain.services.clubConfiguration.ClubConfigurationService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateNewClubProcessHandler implements ClubImportProcessHandler {

    private final ClubService clubService;
    private final ClubDetailsImporter clubDetailsImporter;
    private final ClubConfigurationService clubConfigurationService;

    @Getter
    private final Map<ProcessState, Consumer<ClubImportProcess>> strategies = Map.of(
            ProcessState.CREATE_NEW_CLUB, this::processCreateNewClub,
            ProcessState.FETCHING_CLUB_CREATION_DETAILS, this::processFetchClubCreationDetails,
            ProcessState.FETCHING_CLUB_CREATION_DETAILS_FINISHED, this::processFetchClubCreationDetailsFinished
    );

    private void processFetchClubCreationDetails(ClubImportProcess process) {
        if (process.getDetails().isDone()) {
            process.setState(ProcessState.FETCHING_CLUB_CREATION_DETAILS_FINISHED);
        }
    }


    private void processCreateNewClub(ClubImportProcess process) {
        process.setDetails(this.clubDetailsImporter.createNewClub(process.getClubId()));
        process.setState(ProcessState.FETCHING_CLUB_CREATION_DETAILS);
    }

    private void processFetchClubCreationDetailsFinished(ClubImportProcess process) {
        var result = process.getDetails().join();

        if (result instanceof ClubCreationResult creationResult) {
            this.clubService.createClub(creationResult.getClubDetails(), creationResult.getChampionships());
            process.markDone();

        }

        if (result instanceof FailedClubCreationResult) {
            log.error("IMPORTER - Club {} failed to create as details could not be fetched", process.getClubId());
            this.clubConfigurationService.setClubSync(process.getClubId(), false);
            process.markFailed();
        }

    }
}
