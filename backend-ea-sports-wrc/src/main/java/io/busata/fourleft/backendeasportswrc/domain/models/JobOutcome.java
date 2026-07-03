package io.busata.fourleft.backendeasportswrc.domain.models;

/**
 * What a completed {@link Job} actually did — the branch its import took — recorded for status
 * reporting so a DONE job says more than "DONE". A job reaches {@link JobStatus#DONE} even when the
 * import failed internally and disabled the club's sync ({@link #SYNC_DISABLED}); a hard failure that
 * propagates out of the handler instead ends as {@link JobStatus#FAILED} with no outcome.
 */
public enum JobOutcome {
    /** A club we had never seen was fetched and created. */
    CLUB_CREATED,
    /** An upcoming championship crossed its start — details refreshed and the start announced. */
    CHAMPIONSHIP_STARTED,
    /** An active event finished — boards + standings pushed and the ended event announced. */
    EVENT_ENDED,
    /** Open leaderboards that had new times were pushed. */
    LEADERBOARDS_UPDATED,
    /** A finished event's boards + standings were backfilled (history pass). */
    HISTORY_UPDATED,
    /** A plain club-details refresh with no event/championship transition. */
    DETAILS_REFRESHED,
    /** Nothing needed doing this cycle. */
    NO_CHANGE,
    /** The import failed and the club's sync was disabled; the job still completes. */
    SYNC_DISABLED
}
