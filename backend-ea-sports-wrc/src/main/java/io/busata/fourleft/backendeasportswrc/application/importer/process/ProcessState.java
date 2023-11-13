package io.busata.fourleft.backendeasportswrc.application.importer.process;

import lombok.Getter;

public enum ProcessState {
    START(true),

    UPDATE_EXISTING_CLUB(true),
    FETCHING_CLUB_DETAILS(false),
    FETCHING_CLUB_DETAILS_FINISHED(true),

    CREATE_NEW_CLUB(true),
    FETCHING_CLUB_CREATION_DETAILS(false),
    FETCHING_CLUB_CREATION_DETAILS_FINISHED(true),

    CHECK_LEADERBOARDS(true),
    FETCHING_LEADERBOARDS(false),
    FETCHING_LEADERBOARDS_FINISHED(true),

    UPDATE_EVENT_ENDED(true),
    FETCHING_UPDATE_EVENT_ENDED(false),
    FETCHING_UPDATE_EVENT_ENDED_SUCCESS(true),

    UPDATE_HISTORY(true),
    FETCHING_HISTORY(false),
    FETCHING_HISTORY_SUCCESS(true),

    DONE(false),
    FAILED(true);


    @Getter
    private final boolean keepProcessing;

    ProcessState(boolean keepProcessing) {
        this.keepProcessing = keepProcessing;
    }
}