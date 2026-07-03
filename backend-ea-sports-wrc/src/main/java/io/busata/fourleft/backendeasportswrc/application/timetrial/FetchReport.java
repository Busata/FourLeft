package io.busata.fourleft.backendeasportswrc.application.timetrial;

/**
 * What one board's fetch pass did.
 *
 * @param boardExists    whether the board still existed on Racenet at fetch time
 * @param totalEntries   entries stored (the full board)
 * @param changedEntries entries new or improved since the previous fetch (churn); 0 on first fetch
 */
public record FetchReport(boolean boardExists, int totalEntries, int changedEntries) {

    public static FetchReport missing() {
        return new FetchReport(false, 0, 0);
    }
}
