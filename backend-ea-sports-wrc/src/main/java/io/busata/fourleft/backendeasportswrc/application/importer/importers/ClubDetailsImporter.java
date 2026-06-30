package io.busata.fourleft.backendeasportswrc.application.importer.importers;

import feign.FeignException;
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
        return this.racenetGateway.getClubDetail(clubId).<CompletableFuture<ClubImportResult>>handle((clubDetails, ex) -> {
            if (ex != null) {
                ClubImportFailureReason reason = classifyFailure(ex);
                log.error("IMPORTER: Club creation error, could not get the details for club {} ({})", clubId, reason, ex);
                return CompletableFuture.completedFuture(new FailedClubCreationResult(clubId, reason));
            }
            return getUniqueChampionships(clubDetails)
                    .<ClubImportResult>thenApply(championships -> new ClubCreationResult(clubDetails, championships))
                    .exceptionally(championshipEx -> {
                        log.error("IMPORTER: Club creation error, could not get the championships for club {}", clubId, championshipEx);
                        return new FailedClubCreationResult(clubId, ClubImportFailureReason.UNKNOWN);
                    });
        }).thenCompose(future -> future);
    }

    public CompletableFuture<ClubImportResult> fetchDetails(String clubId) {
        return this.racenetGateway.getClubDetail(clubId).<CompletableFuture<ClubImportResult>>handle((clubDetailsTo, ex) -> {
            if (ex != null) {
                ClubImportFailureReason reason = classifyFailure(ex);
                log.error("IMPORTER: Club update error, could not get the details for club {} ({})", clubId, reason, ex);
                return CompletableFuture.completedFuture(new FailedClubUpdateResult(clubId, reason));
            }
            return getUniqueChampionships(clubDetailsTo)
                    .<ClubImportResult>thenApply(championships -> new ClubDetailsUpdatedResult(clubDetailsTo, championships))
                    .exceptionally(championshipEx -> {
                        log.error("IMPORTER: Club update error, could not get the championships for club {}", clubId, championshipEx);
                        return new FailedClubUpdateResult(clubId, ClubImportFailureReason.UNKNOWN);
                    });
        }).thenCompose(future -> future);
    }

    /**
     * A 404 from the club detail endpoint means the club no longer exists on Racenet, so it
     * should be removed from the sync configuration. Anything else is treated as a (potentially
     * transient) failure that gets retried later.
     */
    private ClubImportFailureReason classifyFailure(Throwable ex) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof FeignException feignException && feignException.status() == 404) {
                return ClubImportFailureReason.CLUB_NOT_FOUND;
            }
        }
        return ClubImportFailureReason.UNKNOWN;
    }

    private CompletableFuture<List<ChampionshipTo>> getUniqueChampionships(ClubDetailsTo clubDetails) {
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
