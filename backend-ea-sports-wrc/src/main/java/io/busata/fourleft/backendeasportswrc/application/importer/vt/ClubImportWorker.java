package io.busata.fourleft.backendeasportswrc.application.importer.vt;

import io.busata.fourleft.api.easportswrc.events.ClubChampionshipStarted;
import io.busata.fourleft.api.easportswrc.events.ClubEventEnded;
import io.busata.fourleft.api.easportswrc.events.LeaderboardUpdatedEvent;
import io.busata.fourleft.backendeasportswrc.application.importer.results.LeaderboardUpdatedResult;
import io.busata.fourleft.backendeasportswrc.application.importer.results.StandingsUpdatedResult;
import io.busata.fourleft.backendeasportswrc.domain.services.club.ClubService;
import io.busata.fourleft.backendeasportswrc.domain.services.clubConfiguration.ClubConfigurationService;
import io.busata.fourleft.backendeasportswrc.domain.services.leaderboards.ClubLeaderboardService;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.BlockingRacenetGateway;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ChampionshipTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ClubDetailsTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.leaderboard.ClubLeaderboardEntryTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.leaderboard.ClubLeaderboardParamsTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.leaderboard.ClubStandingsParamsTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.standings.ClubStandingsResultEntryTo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;

/**
 * The per-club import flow, run on a single virtual thread by {@link VirtualThreadClubImporter}.
 *
 * <p>Each club is imported by a plain, linear, <i>blocking</i> flow: call the Racenet gateway, wait,
 * push the raw result through the existing domain services, re-publish the domain events downstream
 * features depend on. There is no {@code CompletableFuture} state machine and no {@code ProcessState}
 * enum — the legacy {@code process.*} handlers each collapse into one method here.
 *
 * <h2>Flow map (legacy handler → method)</h2>
 * <ul>
 *   <li>{@code InitialClubProcessHandler} → {@link #importClub} (the routing decision).</li>
 *   <li>{@code CreateNewClubProcessHandler} → {@link #createNewClub}.</li>
 *   <li>{@code UpdateClubProcessHandler} → {@link #updateExistingClub} (branches to the two below or
 *       a plain details refresh).</li>
 *   <li>{@code UpcomingChampionshipStartedProcessHandler} → {@link #championshipStarted}.</li>
 *   <li>{@code EventEndedProcessHandler} → {@link #eventEnded}.</li>
 *   <li>{@code UpdateLeaderboardsProcessHandler} → {@link #updateLeaderboards}.</li>
 *   <li>{@code UpdateHistoryClubProcessHandler} → {@link #updateHistory}.</li>
 * </ul>
 *
 * <p><b>Failure model.</b> A failed <i>details</i> fetch is fatal for the cycle: it propagates to
 * {@link #importClub}, which disables sync for the club ({@code setClubSync(clubId, false)}) — the
 * legacy {@code FAILED} behaviour that the batch loop turned into a {@code setClubSync} + Discord
 * "Disabled clubs count" message. A failed <i>single leaderboard or standings</i> fetch is
 * non-fatal: it is logged and skipped so the rest of the batch still applies and the event still
 * fires, matching the legacy per-board {@code exceptionally} handling.
 *
 * <p><b>Batch Discord notification.</b> The legacy per-cycle "Disabled clubs count: N" aggregate has
 * no cycle boundary here (clubs run on independent threads), so it is intentionally dropped; the
 * per-club disable is logged instead.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClubImportWorker {

    private final ClubService clubService;
    private final ClubLeaderboardService clubLeaderboardService;
    private final ClubConfigurationService clubConfigurationService;
    private final BlockingRacenetGateway racenetGateway;
    private final ApplicationEventPublisher eventPublisher;

    public void importClub(String clubId) {
        try {
            if (!clubService.exists(clubId)) {
                createNewClub(clubId);
                return;
            }

            if (clubService.requiresDetailUpdate(clubId)) {
                updateExistingClub(clubId);
            } else if (clubService.requiresLeaderboardUpdate(clubId)) {
                updateLeaderboards(clubId);
            } else if (clubService.requiresHistoryUpdate(clubId)) {
                updateHistory(clubId);
            }
            // else: nothing to do this cycle.
        } catch (Exception ex) {
            handleFailure(clubId, ex);
        }
    }

    private void createNewClub(String clubId) {
        try {
            var clubDetails = this.racenetGateway.getClubDetail(clubId);
            var championships = fetchUniqueChampionships(clubDetails);

            this.clubService.createClub(clubDetails, championships);
        } catch (Exception ex) {
            this.clubConfigurationService.setClubSync(clubId, false);
            log.error("Error while fetching club details for club {} and championships", clubId, ex);
        }
    }

    /**
     * A club needing a detail update splits three ways, mirroring
     * {@code UpdateClubProcessHandler.processUpdateExistingClub}: an upcoming championship that has
     * started and a just-finished active event each get their own batch; otherwise it is a plain
     * details refresh.
     */
    private void updateExistingClub(String clubId) {
        if (clubService.hasUpcomingChampionshipThatStarted(clubId)) {
            championshipStarted(clubId);
        } else if (clubService.hasActiveEventThatFinished(clubId)) {
            eventEnded(clubId);
        } else {
            fetchAndUpdateDetails(clubId);
        }
    }

    /** Mirrors {@code UpcomingChampionshipStartedProcessHandler}: refresh details, then announce the start. */
    private void championshipStarted(String clubId) {
        fetchAndUpdateDetails(clubId);
        eventPublisher.publishEvent(new ClubChampionshipStarted(clubId));
    }

    /**
     * Mirrors {@code EventEndedProcessHandler}: refresh details (fatal on failure), then push the open
     * leaderboards and the active championship's standings, then announce the ended event. A failed
     * board/standings fetch is logged and skipped inside the apply helpers.
     */
    private void eventEnded(String clubId) {
        fetchAndUpdateDetails(clubId);

        applyLeaderboards(clubId, clubService.getOpenLeaderboards(clubId));

        String activeChampionshipId = clubService.getActiveChampionshipId(clubId).orElseThrow();
        applyStandings(clubId, List.of(activeChampionshipId));

        eventPublisher.publishEvent(new ClubEventEnded(clubId));
    }

    /** Mirrors {@code UpdateLeaderboardsProcessHandler}: push the boards that need it, then signal a refresh. */
    private void updateLeaderboards(String clubId) {
        List<String> leaderboards = clubService.getLeaderboardsRequiringUpdate(clubId);
        if (leaderboards.isEmpty()) {
            return;
        }

        applyLeaderboards(clubId, leaderboards);
        eventPublisher.publishEvent(new LeaderboardUpdatedEvent(clubId));
    }

    /**
     * Mirrors {@code UpdateHistoryClubProcessHandler}: push the finished-event leaderboards and their
     * championship standings, mark the history update done, then announce the ended event.
     */
    private void updateHistory(String clubId) {
        applyLeaderboards(clubId, clubService.getHistoryLeaderboards(clubId));
        applyStandings(clubId, clubService.getHistoryChampionships(clubId));

        clubService.markHistoryUpdateDone(clubId);
        eventPublisher.publishEvent(new ClubEventEnded(clubId));
    }

    private void fetchAndUpdateDetails(String clubId) {
        var clubDetails = racenetGateway.getClubDetail(clubId);
        var championships = fetchUniqueChampionships(clubDetails);

        clubService.updateClub(clubDetails, championships);
    }

    /**
     * Fetch every championship the club references except {@code currentChampionship} (already embedded in
     * {@code clubDetails}), one virtual thread per championship. Mirrors the legacy
     * {@code ClubDetailsImporter.getUniqueChampionships} fan-out, but stays on virtual threads instead of the
     * common {@code ForkJoinPool}, so the blocking Racenet calls never pin a carrier thread.
     *
     * <p>If any fetch fails the exception propagates (the caller disables sync), matching the legacy
     * {@code allOf} semantics where one failed fetch failed the whole creation.
     */
    private List<ChampionshipTo> fetchUniqueChampionships(ClubDetailsTo clubDetails) {
        List<String> championshipIds = clubDetails.currentChampionship()
                .map(currentChampionship -> clubDetails.championshipIDs()
                        .stream()
                        .filter(id -> !id.equals(currentChampionship.id()))
                        .toList())
                .orElseGet(clubDetails::championshipIDs);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = championshipIds.stream()
                    .map(id -> executor.submit(() -> racenetGateway.getChampionship(id)))
                    .toList();

            return futures.stream()
                    .map(Futures::join)
                    .filter(Objects::nonNull)
                    .toList();
        }
    }

    /**
     * Fetch each leaderboard on its own virtual thread, then push the results sequentially on this
     * thread (the domain writes stay single-threaded per club). A single board's fetch failure is
     * logged and skipped so the rest of the batch still applies — the legacy per-board
     * {@code exceptionally} behaviour.
     */
    private void applyLeaderboards(String clubId, Collection<String> leaderboardIds) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = leaderboardIds.stream()
                    .map(id -> Map.entry(id, executor.submit(() -> fetchLeaderboard(clubId, id))))
                    .toList();

            for (var future : futures) {
                try {
                    clubLeaderboardService.updateLeaderboards(Futures.join(future.getValue()));
                } catch (Exception ex) {
                    log.error("Club {} • Board {} • Failed to import", clubId, future.getKey(), ex);
                }
            }
        }
    }

    /**
     * Fetch each championship's standings on its own virtual thread, then push the results sequentially.
     * A single championship's fetch failure is logged and skipped, matching the legacy filtering of
     * {@code StandingsImportResultFailed}.
     */
    private void applyStandings(String clubId, Collection<String> championshipIds) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = championshipIds.stream()
                    .map(id -> Map.entry(id, executor.submit(() -> fetchStandings(clubId, id))))
                    .toList();

            for (var future : futures) {
                try {
                    clubService.updateStandings(Futures.join(future.getValue()));
                } catch (Exception ex) {
                    log.error("Club {} • Championship {} • Failed to import standings", clubId, future.getKey(), ex);
                }
            }
        }
    }

    /** Blocking, paginated leaderboard fetch — the sync equivalent of {@code ClubLeaderboardsImporter.importLeaderboard}. */
    private LeaderboardUpdatedResult fetchLeaderboard(String clubId, String leaderboardId) {
        List<ClubLeaderboardEntryTo> entries = new ArrayList<>();
        String cursor = null;
        do {
            var result = racenetGateway.getLeaderboard(clubId, leaderboardId, new ClubLeaderboardParamsTo(cursor));
            entries.addAll(result.entries());
            cursor = StringUtils.isBlank(result.next()) ? null : result.next();
        } while (cursor != null);

        return new LeaderboardUpdatedResult(clubId, leaderboardId, entries);
    }

    /** Blocking, paginated standings fetch — the sync equivalent of {@code ClubStandingsImporter.importStandings}. */
    private StandingsUpdatedResult fetchStandings(String clubId, String championshipId) {
        List<ClubStandingsResultEntryTo> entries = new ArrayList<>();
        String cursor = null;
        do {
            var result = racenetGateway.getStandings(clubId, championshipId, new ClubStandingsParamsTo(cursor));
            entries.addAll(result.entries());
            cursor = StringUtils.isBlank(result.cursorNext()) ? null : result.cursorNext();
        } while (cursor != null);

        return new StandingsUpdatedResult(clubId, championshipId, entries);
    }

    private void handleFailure(String clubId, Exception ex) {
        log.error("Club {} • import failed, disabling sync", clubId, ex);
        clubConfigurationService.setClubSync(clubId, false);
    }
}
