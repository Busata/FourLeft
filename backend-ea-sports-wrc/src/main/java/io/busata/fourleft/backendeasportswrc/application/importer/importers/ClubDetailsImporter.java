package io.busata.fourleft.backendeasportswrc.application.importer.importers;

import io.busata.fourleft.backendeasportswrc.application.importer.results.*;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.RacenetGateway;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ChampionshipTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ClubDetailsTo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClubDetailsImporter {
    private final RacenetGateway racenetGateway;

    public CompletableFuture<ClubImportResult> createNewClub(String clubId) {
        return this.racenetGateway.getClubDetail(clubId).thenCompose(clubDetails -> {
            return getChampionships(clubDetails)
                    .thenApply(championships -> new ClubCreationResult(clubDetails, championships)).thenApply(clubCreationResult -> (ClubImportResult) clubCreationResult);
        }).exceptionally(ex -> {
            log.error("IMPORTER: Club creation error, could not get the details for club {}", clubId, ex);
            return new FailedClubCreationResult(clubId);

        });
    }

    public CompletableFuture<ClubImportResult> fetchDetails(String clubId) {
        return this.racenetGateway.getClubDetail(clubId).thenCompose(clubDetailsTo -> {
            return getChampionships(clubDetailsTo)
                    .thenApply(championships -> new ClubDetailsUpdatedResult(clubDetailsTo, championships))
                    .thenApply(clubUpdateResult -> (ClubImportResult) clubUpdateResult);
        }).exceptionally(ex -> {
            log.error("IMPORTER: Club creation error, could not get the details for club {}", clubId, ex);
            return new FailedClubUpdateResult(clubId);
        });
    }

    private CompletableFuture<List<ChampionshipTo>> getChampionships(ClubDetailsTo clubDetails) {
        List<CompletableFuture<ChampionshipTo>> championships = clubDetails.
                currentChampionship()
                .map(currentChampionship -> {
                    return clubDetails.championshipIDs()
                            .stream()
                            .filter(id -> !id.equals(currentChampionship.id()))
                            .toList();
                })
                .orElseGet(clubDetails::championshipIDs)
                .stream()
                .map(racenetGateway::getChampionship).toList();

        return CompletableFuture.allOf(championships.toArray(CompletableFuture[]::new)).thenApply(v -> {
            return championships.stream().map(CompletableFuture::join)
                    .filter(Objects::nonNull).collect(Collectors.toList());
        });
    }
}
