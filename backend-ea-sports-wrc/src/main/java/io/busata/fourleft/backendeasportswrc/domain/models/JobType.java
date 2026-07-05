package io.busata.fourleft.backendeasportswrc.domain.models;

/**
 * The kinds of work the queue can run. One value per {@code JobHandler}. Add a value + a handler
 * (+ optionally a target) to introduce another job type.
 */
public enum JobType {
    /** Import one club (ref = clubId). */
    CLUB,
    /** Regenerate one club's cached results export (ref = clubId): rebuild + write its summary JSON. */
    CLUB_EXPORT,
    /** Probe every time-trial board for one rally (ref = locationId): does it exist, how many entries. */
    TT_PROBE,
    /** Fetch one time-trial board in full (ref = combinationId): pull + store every entry. */
    TT_FETCH,
    /** Regenerate one time-trial board's CSV export (ref = combinationId): rebuild + write its file. */
    TT_EXPORT
}
