package io.busata.fourleft.backendeasportswrc.application.importer.results;

import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ChampionshipTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ClubDetailsTo;
import lombok.Getter;

import java.util.List;

public class ClubCreationResult extends ClubImportResult {
    @Getter
    private final ClubDetailsTo clubDetails;

    @Getter
    private final List<ChampionshipTo> championships;

    public ClubCreationResult(ClubDetailsTo clubDetailsTo, List<ChampionshipTo> championships) {
        this.clubDetails = clubDetailsTo;
        this.championships = championships;
    }
}
