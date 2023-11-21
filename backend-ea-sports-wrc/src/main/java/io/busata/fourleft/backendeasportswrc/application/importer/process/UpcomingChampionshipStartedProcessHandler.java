package io.busata.fourleft.backendeasportswrc.application.importer.process;

import io.busata.fourleft.api.easportswrc.events.ClubChampionshipStarted;
import io.busata.fourleft.api.easportswrc.events.ClubEventEnded;
import io.busata.fourleft.backendeasportswrc.application.importer.importers.ClubDetailsImporter;
import io.busata.fourleft.backendeasportswrc.application.importer.importers.ClubLeaderboardsImporter;
import io.busata.fourleft.backendeasportswrc.application.importer.importers.ClubStandingsImporter;
import io.busata.fourleft.backendeasportswrc.application.importer.process.core.ClubImportProcess;
import io.busata.fourleft.backendeasportswrc.application.importer.process.core.ClubImportProcessHandler;
import io.busata.fourleft.backendeasportswrc.application.importer.process.core.ProcessState;
import io.busata.fourleft.backendeasportswrc.application.importer.results.ClubDetailsUpdatedResult;
import io.busata.fourleft.backendeasportswrc.application.importer.results.ClubImportResult;
import io.busata.fourleft.backendeasportswrc.application.importer.results.LeaderboardUpdatedResult;
import io.busata.fourleft.backendeasportswrc.application.importer.results.StandingsUpdatedResult;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import io.busata.fourleft.backendeasportswrc.domain.services.leaderboards.ClubLeaderboardService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpcomingChampionshipStartedProcessHandler implements ClubImportProcessHandler {

    private final ApplicationEventPublisher eventPublisher;
    private final ClubService clubService;
    private final ClubDetailsImporter clubDetailsImporter;

    @Getter
    private final Map<ProcessState, Consumer<ClubImportProcess>> strategies = Map.of(
            ProcessState.UPCOMING_CHAMPIONSHIP_STARTED, this::processChampionshipStarted,
            ProcessState.FETCHING_CHAMPIONSHIP, this::processFetchingChampionshipStarted,
            ProcessState.FETCHING_CHAMPIONSHIP_SUCCESS, this::processFetchingChampionshipSuccess

    );

    private void processChampionshipStarted(ClubImportProcess process) {
        process.setDetails(clubDetailsImporter.fetchDetails(process.getClubId()));

        process.setState(ProcessState.FETCHING_CHAMPIONSHIP);
    }

    private void processFetchingChampionshipStarted(ClubImportProcess process) {
        if (process.isDetailsFetchingDone() ) {
            process.setState(ProcessState.FETCHING_CHAMPIONSHIP_SUCCESS);
        }
    }

    private void processFetchingChampionshipSuccess(ClubImportProcess process) {
        ClubImportResult details = process.getDetails().join();

        if (details instanceof ClubDetailsUpdatedResult detailsUpdatedResults) {
            this.clubService.updateClub(detailsUpdatedResults.getClubDetails(), detailsUpdatedResults.getChampionships());
        }

        this.eventPublisher.publishEvent(new ClubChampionshipStarted(process.getClubId()));

        process.markDone();
    }
}
