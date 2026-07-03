package io.busata.fourleft.backendeasportswrc.application.importer.vt;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/** Helpers for blocking on {@link Future}s from the virtual-thread fan-out flows. */
public final class Futures {

    private Futures() {
    }

    /** Block for a fan-out task's result, unwrapping the {@link ExecutionException} so the real cause surfaces. */
    public static <T> T join(Future<T> future) {
        try {
            return future.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for task result", ex);
        } catch (ExecutionException ex) {
            throw new IllegalStateException(ex.getCause());
        }
    }
}
