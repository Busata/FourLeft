package io.busata.fourleft.backendeasportswrc.domain.models;

/**
 * The kinds of work the queue can run. One value per {@code JobHandler}. Add a value + a handler
 * (+ optionally a target) to introduce another job type.
 */
public enum JobType {
    /** Import one club (ref = clubId). */
    CLUB,
    /** Probe every time-trial board for one rally (ref = locationId): does it exist, how many entries. */
    TT_PROBE
}
