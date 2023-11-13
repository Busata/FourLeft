package io.busata.fourleft.backendeasportswrc.application.importer.results;

import lombok.Getter;

public class FailedClubCreationResult extends ClubImportResult {

    @Getter
    private final String clubId;

    public FailedClubCreationResult(String clubId) {
        this.clubId = clubId;
    }
}
