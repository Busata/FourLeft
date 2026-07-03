package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.timetrial;

/**
 * Result of probing one time-trial board.
 *
 * @param boardExists  whether Racenet has a board for this combination
 * @param totalEntries entry count when it exists; {@code null} when it doesn't
 */
public record ProbeResult(boolean boardExists, Integer totalEntries) {

    public static ProbeResult missing() {
        return new ProbeResult(false, null);
    }

    public static ProbeResult exists(int totalEntries) {
        return new ProbeResult(true, totalEntries);
    }
}
