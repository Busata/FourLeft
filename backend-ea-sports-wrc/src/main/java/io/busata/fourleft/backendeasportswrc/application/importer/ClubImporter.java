package io.busata.fourleft.backendeasportswrc.application.importer;

/**
 * Entry point for a club-sync cycle, invoked by {@code ClubUpdateSchedule} every 5s.
 *
 * <p>The seam between the schedule and the importer implementation. The sole implementation is
 * {@code application.importer.vt.VirtualThreadClubImporter}, the linear, blocking,
 * one-virtual-thread-per-club importer.
 */
public interface ClubImporter {

    /**
     * Run one sync cycle: discover syncable clubs and (re)start work for any not already in flight.
     *
     * @return the number of clubs currently being processed (in flight after this cycle).
     */
    int sync();
}
