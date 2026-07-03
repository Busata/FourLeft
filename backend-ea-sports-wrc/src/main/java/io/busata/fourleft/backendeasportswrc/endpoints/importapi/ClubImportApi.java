package io.busata.fourleft.backendeasportswrc.endpoints.importapi;

import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ChampionshipTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ClubDetailsTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.leaderboard.ClubLeaderboardEntryTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.standings.ClubStandingsResultEntryTo;

import java.util.List;

/**
 * PROPOSED write contract for an out-of-process club importer.
 *
 * <p>Today the importer lives in-process ({@code application.importer.*}): it fetches from the
 * Racenet API and calls domain services directly. If the importer moves to a separate app, that
 * app fetches from Racenet and calls these endpoints to push results back. This interface captures
 * the <b>entire persistence write surface</b> of the current importer — nothing else writes.
 *
 * <p><b>Payloads are the raw Racenet response models</b> ({@link ClubDetailsTo},
 * {@link ChampionshipTo}, {@link ClubLeaderboardEntryTo}, {@link ClubStandingsResultEntryTo}). The
 * remote importer already has these after fetching. All the non-trivial logic — championship status
 * transitions, closing deleted championships, leaderboard dedup + cumulative rank/diff, standings
 * dedup-by-rank — stays <b>server-side</b> behind these methods (in {@code ClubFactory} /
 * {@code ClubService} / {@code ClubLeaderboardService}), so the domain model remains authoritative
 * and the importer stays a thin fetch-and-push worker.
 *
 * <p><b>Two things beyond the writes the implementation must honour:</b>
 * <ol>
 *   <li><b>Domain events.</b> Several flows publish events <i>after</i> the writes that downstream
 *       features depend on (Discord notifications; RabbitMQ {@code ChannelUpdatedEvent} via
 *       {@code EventRelayer}). Each method below documents which event its flow currently emits;
 *       the endpoint implementation must re-publish them or those features silently break.</li>
 *   <li><b>What to import is decided by DB reads.</b> The current state machine
 *       ({@code InitialClubProcessHandler} etc.) reads club state to choose which leaderboards /
 *       championships need fetching. This interface is the <i>write</i> half only; if the remote
 *       app also owns that decision logic it needs matching read endpoints (see the design doc).</li>
 * </ol>
 *
 * <p>For a truly decoupled contract the four Racenet {@code *To} types referenced here should move
 * to the shared {@code api} module so both apps compile against them.
 *
 * @see <a href="../../../../../../../../../../docs/importer-write-surface.md">docs/importer-write-surface.md</a>
 */
public interface ClubImportApi {

    /**
     * Create a brand-new club aggregate (club + championships + events + stages + all settings).
     * <p>Maps to {@code ClubService.createClub} (W1). Cascade-inserts the full tree. Emits no event.
     * Called today only when a synced club does not yet exist locally.
     */
    void createClub(CreateClubRequest request);

    /**
     * Update an existing club's details and (re)sync its championships/events.
     * <p>Maps to {@code ClubService.updateClub} (W2): updates name/description/activeMemberCount,
     * upserts championships/events, recomputes statuses, closes championships no longer present in
     * {@code championshipIDs}, and bumps {@code lastDetailsUpdate} / clears the details-required flag.
     * <p>Called from three flows; the event depends on {@code reason}:
     * <ul>
     *   <li>{@link ClubUpdateReason#DETAILS} — plain periodic detail refresh, no event.</li>
     *   <li>{@link ClubUpdateReason#CHAMPIONSHIP_STARTED} — emit {@code ClubChampionshipStarted}.</li>
     *   <li>{@link ClubUpdateReason#EVENT_ENDED} — part of the event-ended flow, which also calls
     *       {@link #updateLeaderboard} + {@link #updateStandings} and then emits {@code ClubEventEnded}
     *       (see {@link #signalEventEnded}).</li>
     * </ul>
     */
    void updateClubDetails(String clubId, UpdateClubRequest request);

    /**
     * Replace a leaderboard's entries with a freshly fetched set.
     * <p>Maps to {@code ClubLeaderboardService.updateLeaderboards} (W7): upserts the
     * {@code ClubLeaderboard}, dedups entries by displayName, sorts by accumulated time and computes
     * cumulative rank + difference server-side, replaces all {@code ClubLeaderboardEntry} rows
     * (orphanRemoval), and stamps {@code Event.lastLeaderboardUpdate} (W4, {@code markBoardAsUpdated}).
     * <p>In the periodic leaderboard-refresh flow this emits {@code LeaderboardUpdatedEvent} (which
     * fans out to RabbitMQ via {@code EventRelayer}). In the event-ended / history flows the event is
     * {@code ClubEventEnded} instead — see {@code signal*} methods.
     */
    void updateLeaderboard(String clubId, String leaderboardId, UpdateLeaderboardRequest request);

    /**
     * Replace one championship's standings.
     * <p>Maps to {@code ClubService.updateStandings} (W5/W6): dedups entries by rank, rebuilds
     * {@code ChampionshipStanding} rows and saves the championship + club. Emits no event on its own;
     * it is part of the event-ended / history flows.
     */
    void updateStandings(String clubId, String championshipId, UpdateStandingsRequest request);

    /**
     * Mark a club's finished championships as history-processed.
     * <p>Maps to {@code ClubService.markHistoryUpdateDone} (W3): sets {@code updatedAfterFinish=true}
     * so they are not re-imported. Call after the history leaderboard/standings pushes; the history
     * flow then emits {@code ClubEventEnded} (see {@link #signalEventEnded}).
     */
    void markHistoryComplete(String clubId);

    /**
     * Enable/disable syncing for a club.
     * <p>Maps to {@code ClubConfigurationService.setClubSync} (W8). The importer calls this with
     * {@code false} on the failure path (details could not be fetched / process failed).
     */
    void setClubSync(String clubId, boolean keepSynced);

    /**
     * Emit {@code ClubEventEnded} for a club. Kept as an explicit signal because the current
     * event-ended and history flows publish this only after a <i>batch</i> of writes
     * (club details + every open leaderboard + active-championship standings). Call once the batch
     * for {@code clubId} has been pushed. Alternatively, fold this into the last write of the batch.
     */
    void signalEventEnded(String clubId);

    // ---------------------------------------------------------------------------------------------
    // Request payloads — thin envelopes around the raw Racenet models the writes consume.
    // ---------------------------------------------------------------------------------------------

    /** @see #createClub */
    record CreateClubRequest(ClubDetailsTo clubDetails, List<ChampionshipTo> championships) {}

    /** @see #updateClubDetails */
    record UpdateClubRequest(ClubDetailsTo clubDetails, List<ChampionshipTo> championships, ClubUpdateReason reason) {}

    /**
     * @see #updateLeaderboard
     * Only {@code entries} crosses the wire; rankAccumulated / differenceAccumulated are derived
     * server-side. Per entry the write consumes: displayName, nationalityID, platform, rank, vehicle,
     * wrcPlayerId, ssid, time, timePenalty, timeAccumulated, differenceToFirst.
     */
    record UpdateLeaderboardRequest(List<ClubLeaderboardEntryTo> entries) {}

    /**
     * @see #updateStandings
     * Per entry the write consumes: ssid, displayName, pointsAccumulated, rank, nationalityID.
     */
    record UpdateStandingsRequest(List<ClubStandingsResultEntryTo> entries) {}

    /** Distinguishes which flow triggered {@link #updateClubDetails}, so the right event is emitted. */
    enum ClubUpdateReason {
        /** Periodic detail refresh — no event. */
        DETAILS,
        /** An upcoming championship became active — emit {@code ClubChampionshipStarted}. */
        CHAMPIONSHIP_STARTED,
        /** Part of the active-event-finished batch — event handled by {@link #signalEventEnded}. */
        EVENT_ENDED
    }
}
