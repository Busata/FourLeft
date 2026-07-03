package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.timetrial;

/**
 * Fetches a single Racenet time-trial leaderboard, addressed by the same tuple as the public URL
 * {@code /ea_sports_wrc/leaderboards/?selectedLocation=…&selectedRoute=…&selectedSurfaceCondition=…&selectedVehicleClass=…}.
 *
 * <p>The probe only needs to know whether the board exists and its entry count; the full-entries
 * fetch worker will reuse (or extend) this seam later.
 */
public interface TimeTrialBoardGateway {

    /**
     * Probe one board.
     *
     * @param surfaceCondition 0 = dry, 1 = wet
     * @return existence + entry count (entry count null / ignored when the board doesn't exist)
     */
    ProbeResult probe(long locationId, long routeId, int surfaceCondition, long vehicleClassId);
}
