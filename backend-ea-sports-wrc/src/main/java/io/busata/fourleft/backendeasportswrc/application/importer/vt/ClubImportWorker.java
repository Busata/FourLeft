package io.busata.fourleft.backendeasportswrc.application.importer.vt;

import io.busata.fourleft.api.easportswrc.events.ClubChampionshipStarted;
import io.busata.fourleft.api.easportswrc.events.ClubEventEnded;
import io.busata.fourleft.api.easportswrc.events.LeaderboardUpdatedEvent;
import io.busata.fourleft.backendeasportswrc.application.importer.results.LeaderboardUpdatedResult;
import io.busata.fourleft.backendeasportswrc.application.importer.results.StandingsUpdatedResult;
import io.busata.fourleft.backendeasportswrc.domain.models.JobOutcome;
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
 * push the raw result through the domain services, re-publish the domain events downstream features
 * depend on. {@link #importClub} routes a club to one of a handful of single-purpose methods
 * ({@link #createNewClub}, {@link #updateExistingClub}, {@link #updateLeaderboards},
 * {@link #updateHistory}) based on what the club currently needs.
 *
 * <p><b>Failure model.</b> A failed <i>details</i> fetch is fatal for the cycle: it propagates to
 * {@link #importClub}, which disables sync for the club ({@code setClubSync(clubId, false)}). A
 * failed <i>single leaderboard or standings</i> fetch is non-fatal: it is logged and skipped so the
 * rest of the batch still applies and the event still fires.
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

    public ClubImportReport importClub(String clubId) {
        try {
            if (!clubService.exists(clubId)) {
                return createNewClub(clubId);
            }

            if (clubService.requiresDetailUpdate(clubId)) {
                return updateExistingClub(clubId);
            } else if (clubService.requiresLeaderboardUpdate(clubId)) {
                return updateLeaderboards(clubId);
            } else if (clubService.requiresHistoryUpdate(clubId)) {
                return updateHistory(clubId);
            }
            // Nothing to do this cycle.
            return ClubImportReport.of(JobOutcome.NO_CHANGE, false);
        } catch (Exception ex) {
            handleFailure(clubId, ex);
            return ClubImportReport.of(JobOutcome.SYNC_DISABLED, false);
        }
    }

    private ClubImportReport createNewClub(String clubId) {
        try {
            var clubDetails = this.racenetGateway.getClubDetail(clubId);
            var championships = fetchUniqueChampionships(clubDetails);

            this.clubService.createClub(clubDetails, championships);
            return ClubImportReport.of(JobOutcome.CLUB_CREATED, true);
        } catch (Exception ex) {
            this.clubConfigurationService.setClubSync(clubId, false);
            log.error("Error while fetching club details for club {} and championships", clubId, ex);
            return ClubImportReport.of(JobOutcome.SYNC_DISABLED, false);
        }
    }

    /**
     * A club needing a detail update splits three ways: an upcoming championship that has started and
     * a just-finished active event each get their own batch; otherwise it is a plain details refresh.
     */
    private ClubImportReport updateExistingClub(String clubId) {
        if (clubService.hasUpcomingChampionshipThatStarted(clubId)) {
            return championshipStarted(clubId);
        } else if (clubService.hasActiveEventThatFinished(clubId)) {
            return eventEnded(clubId);
        } else {
            fetchAndUpdateDetails(clubId);
            return ClubImportReport.of(JobOutcome.DETAILS_REFRESHED, true);
        }
    }

    /** Refresh details, then announce that the upcoming championship has started. */
    private ClubImportReport championshipStarted(String clubId) {
        fetchAndUpdateDetails(clubId);
        eventPublisher.publishEvent(new ClubChampionshipStarted(clubId));
        return ClubImportReport.of(JobOutcome.CHAMPIONSHIP_STARTED, true);
    }

    /**
     * Refresh details (fatal on failure), then push the open leaderboards and the active championship's
     * standings, then announce the ended event. A failed board/standings fetch is logged and skipped
     * inside the apply helpers.
     */
    private ClubImportReport eventEnded(String clubId) {
        fetchAndUpdateDetails(clubId);

        AppliedBoards boards = applyLeaderboards(clubId, clubService.getOpenLeaderboards(clubId));

        String activeChampionshipId = clubService.getActiveChampionshipId(clubId).orElseThrow();
        int standings = applyStandings(clubId, List.of(activeChampionshipId));

        eventPublisher.publishEvent(new ClubEventEnded(clubId));
        return new ClubImportReport(JobOutcome.EVENT_ENDED, true,
                boards.boards(), standings, boards.entries());
    }

    /** Push the boards that need it, then signal a refresh. */
    private ClubImportReport updateLeaderboards(String clubId) {
        List<String> leaderboards = clubService.getLeaderboardsRequiringUpdate(clubId);
        if (leaderboards.isEmpty()) {
            return ClubImportReport.of(JobOutcome.NO_CHANGE, false);
        }

        AppliedBoards boards = applyLeaderboards(clubId, leaderboards);
        eventPublisher.publishEvent(new LeaderboardUpdatedEvent(clubId));
        return new ClubImportReport(JobOutcome.LEADERBOARDS_UPDATED, true,
                boards.boards(), 0, boards.entries());
    }

    /**
     * Push the finished-event leaderboards and their championship standings, mark the history update
     * done, then announce the ended event.
     */
    private ClubImportReport updateHistory(String clubId) {
        AppliedBoards boards = applyLeaderboards(clubId, clubService.getHistoryLeaderboards(clubId));
        int standings = applyStandings(clubId, clubService.getHistoryChampionships(clubId));

        clubService.markHistoryUpdateDone(clubId);
        eventPublisher.publishEvent(new ClubEventEnded(clubId));
        return new ClubImportReport(JobOutcome.HISTORY_UPDATED, true,
                boards.boards(), standings, boards.entries());
    }

    private void fetchAndUpdateDetails(String clubId) {
        var clubDetails = racenetGateway.getClubDetail(clubId);
        var championships = fetchUniqueChampionships(clubDetails);

        clubService.updateClub(clubDetails, championships);
    }

    /**
     * Fetch every championship the club references except {@code currentChampionship} (already embedded in
     * {@code clubDetails}), one virtual thread per championship, so the blocking Racenet calls never pin a
     * carrier thread.
     *
     * <p>If any fetch fails the exception propagates and the caller disables sync — one failed fetch fails
     * the whole creation.
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
     * logged and skipped so the rest of the batch still applies.
     */
    private AppliedBoards applyLeaderboards(String clubId, Collection<String> leaderboardIds) {
        int boards = 0;
        int entries = 0;
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = leaderboardIds.stream()
                    .map(id -> Map.entry(id, executor.submit(() -> fetchLeaderboard(clubId, id))))
                    .toList();

            for (var future : futures) {
                try {
                    LeaderboardUpdatedResult result = Futures.join(future.getValue());
                    clubLeaderboardService.updateLeaderboards(result);
                    boards++;
                    entries += result.getEntries().size();
                } catch (Exception ex) {
                    log.error("Club {} • Board {} • Failed to import", clubId, future.getKey(), ex);
                }
            }
        }
        return new AppliedBoards(boards, entries);
    }

    /** How much a leaderboard batch actually pushed: distinct boards and total entries across them. */
    private record AppliedBoards(int boards, int entries) {
    }

    /**
     * Fetch each championship's standings on its own virtual thread, then push the results sequentially.
     * A single championship's fetch failure is logged and skipped.
     */
    private int applyStandings(String clubId, Collection<String> championshipIds) {
        int applied = 0;
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = championshipIds.stream()
                    .map(id -> Map.entry(id, executor.submit(() -> fetchStandings(clubId, id))))
                    .toList();

            for (var future : futures) {
                try {
                    clubService.updateStandings(Futures.join(future.getValue()));
                    applied++;
                } catch (Exception ex) {
                    log.error("Club {} • Championship {} • Failed to import standings", clubId, future.getKey(), ex);
                }
            }
        }
        return applied;
    }

    /** Blocking, paginated leaderboard fetch. */
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

    /** Blocking, paginated standings fetch. */
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
