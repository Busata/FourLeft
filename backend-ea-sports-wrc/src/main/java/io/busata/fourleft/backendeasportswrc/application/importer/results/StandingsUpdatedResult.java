package io.busata.fourleft.backendeasportswrc.application.importer.results;

import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.standings.ClubStandingsResultEntryTo;
import lombok.Getter;

import java.util.List;

public class StandingsUpdatedResult extends StandingsImportResult {

    @Getter
    private final String clubId;

    @Getter
    private final String championshipId;

    @Getter
    private final List<ClubStandingsResultEntryTo> entries;

    public StandingsUpdatedResult(String clubId, String championshipId, List<ClubStandingsResultEntryTo> entries) {
        this.clubId = clubId;
        this.championshipId = championshipId;
        this.entries = entries;
    }
}
