package io.busata.fourleft.backendeasportswrc.application.importer.vt;

import io.busata.fourleft.backendeasportswrc.domain.models.JobOutcome;

/**
 * What a single {@link ClubImportWorker#importClub} run actually did, surfaced for job status
 * reporting. Kept in the importer package so the importer stays decoupled from the work-queue layer;
 * {@code ClubImportJobHandler} maps it onto a {@code JobResult}.
 *
 * @param outcome             the branch the import took
 * @param changed             whether it altered stored data (vs a no-op refresh)
 * @param leaderboardsUpdated number of leaderboards successfully pushed
 * @param standingsUpdated    number of championship standings successfully pushed
 * @param entriesImported     total leaderboard entries fetched across those boards
 */
public record ClubImportReport(JobOutcome outcome, boolean changed,
                               int leaderboardsUpdated, int standingsUpdated, int entriesImported) {

    /** A report with no item counts (create/refresh/no-op/failure outcomes). */
    static ClubImportReport of(JobOutcome outcome, boolean changed) {
        return new ClubImportReport(outcome, changed, 0, 0, 0);
    }
}
