package io.busata.fourleft.backendeasportswrc.application.importer.results;

import lombok.Getter;

public class FailedClubUpdateResult extends ClubImportResult {

    @Getter
    private final String clubId;


    public FailedClubUpdateResult(String clubId) {
        this.clubId = clubId;
    }
}
