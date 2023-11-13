package io.busata.fourleft.backendeasportswrc.application.importer.results;

import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ChampionshipTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ClubDetailsTo;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
public class ClubDetailsUpdatedResult extends ClubImportResult {

    @Getter
    private final ClubDetailsTo clubDetails;

    @Getter
    private final List<ChampionshipTo> championships;

}
