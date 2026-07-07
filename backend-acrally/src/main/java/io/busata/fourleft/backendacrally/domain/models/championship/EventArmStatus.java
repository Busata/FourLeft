package io.busata.fourleft.backendacrally.domain.models.championship;

/**
 * Lifecycle of an {@link EventArm}. A user has at most one live ({@code ARMED}/{@code BOUND}) arm.
 * <ul>
 *   <li>{@code ARMED} — pressed Start; waiting for the next run to open.</li>
 *   <li>{@code BOUND} — attached to a freshly opened session (the run in progress).</li>
 *   <li>{@code CONSUMED} — that run finished; see {@link EventArmOutcome} for whether it scored.</li>
 *   <li>{@code CANCELLED} — disarmed, or superseded by arming a different stage.</li>
 * </ul>
 */
public enum EventArmStatus {
    ARMED,
    BOUND,
    CONSUMED,
    CANCELLED
}
