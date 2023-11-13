package io.busata.fourleft.backendeasportswrc.application.importer.importers;

import io.busata.fourleft.backendeasportswrc.application.importer.results.LeaderboardImportResult;
import io.busata.fourleft.backendeasportswrc.application.importer.results.LeaderboardImportResultFailed;
import io.busata.fourleft.backendeasportswrc.application.importer.results.LeaderboardUpdatedResult;

import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.RacenetGateway;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.leaderboard.ClubLeaderboardEntryTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.leaderboard.ClubLeaderboardParamsTo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClubLeaderboardsImporter {

    private final RacenetGateway racenetGateway;


    public CompletableFuture<LeaderboardImportResult> importLeaderboard(String clubId, String leaderboardId) {
        ClubLeaderboardParamsTo params = new ClubLeaderboardParamsTo(null);

        return racenetGateway.getLeaderboard(clubId, leaderboardId, params)
                .thenCompose(result -> {
                    List<ClubLeaderboardEntryTo> entries = new ArrayList<>(result.entries());

                    if(StringUtils.isBlank(result.next())) {
                        return CompletableFuture.completedFuture(entries);
                    } else {
                        return getRemainingEntries(clubId, leaderboardId, new ClubLeaderboardParamsTo(result.next()), entries);
                    }
                })
                .thenApply(result -> new LeaderboardUpdatedResult(clubId, leaderboardId, result))
                .thenApply(results -> (LeaderboardImportResult) results)
                .exceptionally(ex -> {
                    return new LeaderboardImportResultFailed(clubId, leaderboardId, ex.getMessage());
                });
    }

    private CompletableFuture<List<ClubLeaderboardEntryTo>> getRemainingEntries(String clubId, String leaderboardId, ClubLeaderboardParamsTo params, List<ClubLeaderboardEntryTo> entriesSoFar) {
        return racenetGateway.getLeaderboard(clubId, leaderboardId, params).thenCompose(result -> {
            entriesSoFar.addAll(result.entries());
            if(StringUtils.isBlank(result.next())) {
                return CompletableFuture.completedFuture(entriesSoFar);
            } else {
                return getRemainingEntries(clubId, leaderboardId, new ClubLeaderboardParamsTo(result.next()), entriesSoFar);
            }
        });
    }


}
