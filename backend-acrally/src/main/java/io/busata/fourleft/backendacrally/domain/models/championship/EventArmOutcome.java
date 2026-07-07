package io.busata.fourleft.backendacrally.domain.models.championship;

/** Why a consumed {@link EventArm}'s run did or didn't land on the leaderboard. */
public enum EventArmOutcome {
    /** The run matched and set a new best — an {@code event_entry} was written/updated. */
    RECORDED,
    /** The run matched but an existing time was as good or better, so nothing changed. */
    SLOWER,
    /** The stage driven wasn't the armed variant. */
    WRONG_STAGE,
    /** The car wasn't one the event permits. */
    WRONG_CAR,
    /** The event's window had closed by the time the result arrived. */
    EVENT_CLOSED
}
