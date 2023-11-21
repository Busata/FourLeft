package io.busata.fourleft.backendeasportswrc.application.importer.process;

import io.busata.fourleft.backendeasportswrc.application.importer.importers.ClubLeaderboardsImporter;
import io.busata.fourleft.backendeasportswrc.application.importer.importers.ClubStandingsImporter;
import io.busata.fourleft.backendeasportswrc.application.importer.process.core.ClubImportProcess;
import io.busata.fourleft.backendeasportswrc.application.importer.process.core.ClubImportProcessHandler;
import io.busata.fourleft.backendeasportswrc.application.importer.process.core.ProcessState;
import io.busata.fourleft.backendeasportswrc.application.importer.results.LeaderboardUpdatedResult;
import io.busata.fourleft.backendeasportswrc.application.importer.results.StandingsUpdatedResult;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import io.busata.fourleft.backendeasportswrc.domain.services.leaderboards.ClubLeaderboardService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class UpdateHistoryClubProcessHandler implements ClubImportProcessHandler {

    private final ClubService clubService;
    private final ClubLeaderboardsImporter leaderboardsImporter;
    private final ClubStandingsImporter clubStandingsImporter;
    private final ClubLeaderboardService leaderboardService;


    @Getter
    private final Map<ProcessState, Consumer<ClubImportProcess>> strategies = Map.of(
            ProcessState.UPDATE_HISTORY, this::processUpdateHistory,
            ProcessState.FETCHING_HISTORY, this::processFetchingHistory,
            ProcessState.FETCHING_HISTORY_SUCCESS, this::processFetchingHistorySuccess
    );

    private void processUpdateHistory(ClubImportProcess process) {
        Set<String> leaderboards = this.clubService.getHistoryLeaderboards(process.getClubId());
        Set<String> championships = this.clubService.getHistoryChampionships(process.getClubId());


        process.setLeaderboards(leaderboards.stream().map(leaderboardId -> {
            return leaderboardsImporter.importLeaderboard(process.getClubId(), leaderboardId);
        }).toList());


        process.setStandings(
                championships.stream().map(championshipId -> {
                    return clubStandingsImporter.importStandings(process.getClubId(), championshipId);
                }).toList()
        );

        process.setState(ProcessState.FETCHING_HISTORY);
    }

    private void processFetchingHistory(ClubImportProcess process) {
        if (process.isLeaderboardFetchingDone() && process.isStandingsFetchingDone()) {
            process.setState(ProcessState.FETCHING_HISTORY_SUCCESS);
        }
    }

    private void processFetchingHistorySuccess(ClubImportProcess process) {
        process.getLeaderboards().stream()
                .map(CompletableFuture::join)
                .filter(LeaderboardUpdatedResult.class::isInstance)
                .map(LeaderboardUpdatedResult.class::cast)
                .forEach(this.leaderboardService::updateLeaderboards);


        process.getStandings().stream().map(CompletableFuture::join)
                .filter(StandingsUpdatedResult.class::isInstance)
                .map(StandingsUpdatedResult.class::cast)
                .forEach(this.clubService::updateStandings);

        this.clubService.markHistoryUpdateDone(process.getClubId());


        process.markDone();

    }
}
