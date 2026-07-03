package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.timetrial;

/**
 * Summary of a full board fetch. The rows themselves are streamed to the caller's page consumer as
 * they arrive (so a huge board never sits fully in memory); this only carries the envelope facts.
 *
 * @param boardExists  whether Racenet has a board for this combination
 * @param totalEntries entrant count Racenet reports on the envelope
 */
public record FetchResult(boolean boardExists, int totalEntries) {

    public static FetchResult missing() {
        return new FetchResult(false, 0);
    }

    public static FetchResult exists(int totalEntries) {
        return new FetchResult(true, totalEntries);
    }
}
