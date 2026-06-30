package io.busata.fourleft.backendeasportswrc.application.importer.results;

import lombok.Getter;

public class FailedClubUpdateResult extends ClubImportResult {

    @Getter
    private final String clubId;

    @Getter
    private final ClubImportFailureReason reason;


    public FailedClubUpdateResult(String clubId) {
        this(clubId, ClubImportFailureReason.UNKNOWN);
    }

    public FailedClubUpdateResult(String clubId, ClubImportFailureReason reason) {
        this.clubId = clubId;
        this.reason = reason;
    }
}
