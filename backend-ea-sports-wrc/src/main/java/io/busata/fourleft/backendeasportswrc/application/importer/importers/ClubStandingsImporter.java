package io.busata.fourleft.backendeasportswrc.application.importer.importers;

import io.busata.fourleft.backendeasportswrc.application.importer.results.*;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.RacenetGateway;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.leaderboard.ClubStandingsParamsTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.standings.ClubStandingsResultEntryTo;
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
public class ClubStandingsImporter {

    private final RacenetGateway racenetGateway;


    public CompletableFuture<StandingsImportResult> importStandings(String clubId, String championshipId) {
        ClubStandingsParamsTo params = new ClubStandingsParamsTo(null);
        return racenetGateway.getStandings(clubId, championshipId, params)
                .thenCompose(result -> {

                    List<ClubStandingsResultEntryTo> entries = new ArrayList<>(result.entries());

                    if(StringUtils.isBlank(result.cursorNext())) {
                        return CompletableFuture.completedFuture(entries);
                    } else {
                        return getRemainingEntries(clubId, championshipId, new ClubStandingsParamsTo(result.cursorNext()), entries);
                    }
                })
                .thenApply(result -> new StandingsUpdatedResult(clubId, championshipId, result))
                .thenApply(results -> (StandingsImportResult) results)
                .exceptionally(ex -> {
                    return new StandingsImportResultFailed(clubId, championshipId, ex.getMessage());
                });
    }

    private CompletableFuture<List<ClubStandingsResultEntryTo>> getRemainingEntries(String clubId, String championshipId, ClubStandingsParamsTo params, List<ClubStandingsResultEntryTo> entriesSoFar) {
        return racenetGateway.getStandings(clubId, championshipId, params).thenCompose(result -> {

            entriesSoFar.addAll(result.entries());
            if(StringUtils.isBlank(result.cursorNext())) {
                return CompletableFuture.completedFuture(entriesSoFar);
            } else {
                return getRemainingEntries(clubId, championshipId, new ClubStandingsParamsTo(result.cursorNext()), entriesSoFar);
            }
        });
    }


}
