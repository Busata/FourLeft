package io.busata.fourleft.backendacrally.domain.models.championship;

/**
 * Lifecycle of an {@link EventArm}. A user has at most one live ({@code ARMED}/{@code BOUND}) arm.
 * <ul>
 *   <li>{@code ARMED} — pressed Start; waiting for the next run to open.</li>
 *   <li>{@code BOUND} — attached to a freshly opened session (the run in progress). Final: it
 *       resolves as a result or a DNF, never back to {@code ARMED}.</li>
 *   <li>{@code CONSUMED} — that run resolved; see {@link EventArmOutcome} for whether it scored
 *       (abandoned runs — restart, quit, crash — consume as {@code DNF}).</li>
 *   <li>{@code CANCELLED} — disarmed, or superseded by arming a different stage.</li>
 *   <li>{@code EXPIRED} — armed but no run ever completed; timed out as a DNF. Without this a
 *       forgotten arm waits forever and captures whatever run the driver starts days later.</li>
 * </ul>
 */
public enum EventArmStatus {
    ARMED,
    BOUND,
    CONSUMED,
    CANCELLED,
    EXPIRED
}
