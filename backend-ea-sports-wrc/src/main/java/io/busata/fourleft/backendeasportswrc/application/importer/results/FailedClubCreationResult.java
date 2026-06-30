package io.busata.fourleft.backendeasportswrc.application.importer.results;

import lombok.Getter;

public class FailedClubCreationResult extends ClubImportResult {

    @Getter
    private final String clubId;

    @Getter
    private final ClubImportFailureReason reason;

    public FailedClubCreationResult(String clubId) {
        this(clubId, ClubImportFailureReason.UNKNOWN);
    }

    public FailedClubCreationResult(String clubId, ClubImportFailureReason reason) {
        this.clubId = clubId;
        this.reason = reason;
    }
}
