package io.busata.fourleft.backendeasportswrc.application.importer.queue;

/**
 * Outcome a {@link JobHandler} reports back to the worker.
 *
 * @param changed whether this run actually imported new/different data. Drives the
 *                adaptive cadence of the originating target: changed -> refresh sooner,
 *                unchanged -> back off. Ignored for targets with a fixed cadence.
 */
public record JobResult(boolean changed) {

    public static JobResult dataChanged() {
        return new JobResult(true);
    }

    public static JobResult noChange() {
        return new JobResult(false);
    }
}
