package io.busata.fourleft.backendeasportswrc.application.work.queue;

import io.busata.fourleft.backendeasportswrc.domain.models.JobOutcome;

/**
 * Outcome a {@link JobHandler} reports back to the worker, recorded on the job for status reporting.
 *
 * @param changed             whether this run actually imported new/different data. Drives the
 *                            adaptive cadence of the originating target: changed -> refresh sooner,
 *                            unchanged -> back off. Ignored for targets with a fixed cadence.
 * @param outcome             what the run did (which import branch it took)
 * @param leaderboardsUpdated number of leaderboards pushed this run
 * @param standingsUpdated    number of championship standings pushed this run
 * @param entriesImported     total leaderboard entries fetched across those boards
 */
public record JobResult(boolean changed, JobOutcome outcome,
                        int leaderboardsUpdated, int standingsUpdated, int entriesImported) {

    public static JobResult dataChanged() {
        return new JobResult(true, JobOutcome.DETAILS_REFRESHED, 0, 0, 0);
    }

    public static JobResult noChange() {
        return new JobResult(false, JobOutcome.NO_CHANGE, 0, 0, 0);
    }
}
