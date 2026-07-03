package io.busata.fourleft.backendeasportswrc.application.timetrial;

/**
 * What one board's fetch pass did.
 *
 * @param boardExists    whether the board still existed on Racenet at fetch time
 * @param storedEntries  entries actually stored — capped at the fetch limit (the top N)
 * @param boardSize      the board's true entrant count from Racenet, which may exceed {@link #storedEntries}
 * @param changedEntries entries new or improved since the previous fetch (churn); 0 on first fetch
 */
public record FetchReport(boolean boardExists, int storedEntries, int boardSize, int changedEntries) {

    public static FetchReport missing() {
        return new FetchReport(false, 0, 0, 0);
    }
}
