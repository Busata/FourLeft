package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.timetrial;

import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.timetrial.TimeTrialLeaderboardEntryTo;

import java.util.List;
import java.util.function.Consumer;

/**
 * Fetches a single Racenet time-trial leaderboard, addressed by the same tuple as the public URL
 * {@code /ea_sports_wrc/leaderboards/?selectedLocation=…&selectedRoute=…&selectedSurfaceCondition=…&selectedVehicleClass=…}.
 *
 * <p>Two reads over the same board, sharing one paging seam: {@link #probe} asks for a single row
 * (existence + total count only), {@link #fetch} walks every page.
 */
public interface TimeTrialBoardGateway {

    /**
     * Probe one board — a single-row read.
     *
     * @param surfaceCondition 0 = dry, 1 = wet
     * @return existence + entry count (entry count null / ignored when the board doesn't exist)
     */
    ProbeResult probe(long locationId, long routeId, int surfaceCondition, long vehicleClassId);

    /**
     * Fetch a board's top entries, streaming each page to {@code pageConsumer} as it arrives — the
     * board is never held in memory here, so it costs one page at a time. Pages via the cursor until
     * {@code maxEntries} is reached or the board runs out. The consumer is not called when the board
     * doesn't exist (404). The envelope's total entrant count is still returned even when capped, so
     * callers know the true board size beyond what was fetched.
     *
     * @param surfaceCondition 0 = dry, 1 = wet
     * @param maxEntries       stop after roughly this many entries; {@code <= 0} means the whole board
     * @param pageConsumer     invoked once per non-empty page, in leaderboard order
     * @return existence + the envelope's total entrant count (the full size, not the capped count)
     */
    FetchResult fetch(long locationId, long routeId, int surfaceCondition, long vehicleClassId,
                      int maxEntries, Consumer<List<TimeTrialLeaderboardEntryTo>> pageConsumer);
}
