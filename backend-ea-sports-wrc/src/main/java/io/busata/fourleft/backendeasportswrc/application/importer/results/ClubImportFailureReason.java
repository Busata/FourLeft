package io.busata.fourleft.backendeasportswrc.application.importer.results;

public enum ClubImportFailureReason {
    /**
     * The Racenet API returned a 404 for the club: it no longer exists and should
     * be removed from the sync configuration rather than retried.
     */
    CLUB_NOT_FOUND,

    /**
     * Any other (potentially transient) failure: the club is disabled but kept around
     * so it can be retried on the next periodic reset.
     */
    UNKNOWN
}
