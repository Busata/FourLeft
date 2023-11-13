package io.busata.fourleft.backendeasportswrc.application.importer.results;

import lombok.Getter;

@Getter
public class StandingsImportResultFailed extends StandingsImportResult {

    private final String clubId;
    private final String championshipId;
    private final String message;

    public StandingsImportResultFailed(String clubId, String championshipId, String message) {
        this.clubId = clubId;
        this.championshipId = championshipId;
        this.message = message;
    }
}
