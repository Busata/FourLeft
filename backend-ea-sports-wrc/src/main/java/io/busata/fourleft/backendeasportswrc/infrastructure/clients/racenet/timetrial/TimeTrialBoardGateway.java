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
     * Fetch one board in full, streaming each page to {@code pageConsumer} as it arrives — the whole
     * board is never held in memory here, so a 50k-entry board costs one page at a time. Pages via
     * the cursor until it runs out. The consumer is not called when the board doesn't exist (404).
     *
     * @param surfaceCondition 0 = dry, 1 = wet
     * @param pageConsumer      invoked once per non-empty page, in leaderboard order
     * @return existence + the envelope's total entrant count
     */
    FetchResult fetch(long locationId, long routeId, int surfaceCondition, long vehicleClassId,
                      Consumer<List<TimeTrialLeaderboardEntryTo>> pageConsumer);
}
