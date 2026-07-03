package io.busata.fourleft.backendeasportswrc.application.importer;

/**
 * Entry point for a club-sync cycle, invoked by {@code ClubUpdateSchedule} every 5s.
 *
 * <p>This is the seam that lets the importer implementation be swapped without touching the
 * schedule. Pick the implementation with the {@code fourleft.importer.mode} property:
 * <ul>
 *   <li>{@code legacy} (default) — {@link ClubsImporterService}, the {@code CompletableFuture}
 *       state machine in {@code application.importer.process.*}.</li>
 *   <li>{@code virtual-threads} — {@code application.importer.vt.VirtualThreadClubImporter},
 *       the linear, blocking, one-virtual-thread-per-club rewrite.</li>
 * </ul>
 * Exactly one implementation is active at a time (see the {@code @ConditionalOnProperty} on each).
 */
public interface ClubImporter {

    /**
     * Run one sync cycle: discover syncable clubs and (re)start work for any not already in flight.
     *
     * @return the number of clubs currently being processed (in flight after this cycle).
     */
    int sync();
}
