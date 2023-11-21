package io.busata.fourleft.backendeasportswrc.application.importer.process;

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

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventEndedProcessHandler implements ClubImportProcessHandler {

    private final ApplicationEventPublisher eventPublisher;
    private final ClubService clubService;
    private final ClubLeaderboardService leaderboardService;
    private final ClubLeaderboardsImporter leaderboardsImporter;
    private final ClubStandingsImporter clubStandingsImporter;
    private final ClubDetailsImporter clubDetailsImporter;

    @Getter
    private final Map<ProcessState, Consumer<ClubImportProcess>> strategies = Map.of(
            ProcessState.UPDATE_EVENT_ENDED, this::processUpdateEventEnded,
            ProcessState.FETCHING_UPDATE_EVENT_ENDED, this::processFetchingUpdateEventEnded,
            ProcessState.FETCHING_UPDATE_EVENT_ENDED_SUCCESS, this::processFetchingUpdateEventEndedSuccess
    );

    private void processUpdateEventEnded(ClubImportProcess process) {
        process.setDetails(clubDetailsImporter.fetchDetails(process.getClubId()));

        List<String> leaderboards = this.clubService.getOpenLeaderboards(process.getClubId());
        process.setLeaderboards(leaderboards.stream().map(leaderboardId -> {
            return leaderboardsImporter.importLeaderboard(process.getClubId(), leaderboardId);
        }).toList());


        process.setStandings(List.of(
                clubStandingsImporter.importStandings(process.getClubId(), clubService.getActiveChampionshipId(process.getClubId()).orElseThrow())
        ));

        process.setState(ProcessState.FETCHING_UPDATE_EVENT_ENDED);
    }

    private void processFetchingUpdateEventEnded(ClubImportProcess process) {
        if (process.isDetailsFetchingDone() && process.isLeaderboardFetchingDone() && process.isStandingsFetchingDone()) {
            process.setState(ProcessState.FETCHING_UPDATE_EVENT_ENDED_SUCCESS);
        }
    }

    private void processFetchingUpdateEventEndedSuccess(ClubImportProcess process) {
        ClubImportResult details = process.getDetails().join();

        if (details instanceof ClubDetailsUpdatedResult detailsUpdatedResults) {
            this.clubService.updateClub(detailsUpdatedResults.getClubDetails(), detailsUpdatedResults.getChampionships());
        }

        process.getLeaderboards().stream()
                .map(CompletableFuture::join)
                .filter(LeaderboardUpdatedResult.class::isInstance)
                .map(LeaderboardUpdatedResult.class::cast)
                .forEach(this.leaderboardService::updateLeaderboards);


        process.getStandings().stream().map(CompletableFuture::join)
                .filter(StandingsUpdatedResult.class::isInstance)
                .map(StandingsUpdatedResult.class::cast)
                .forEach(this.clubService::updateStandings);

        this.eventPublisher.publishEvent(new ClubEventEnded(process.getClubId()));

        process.markDone();
    }
}
