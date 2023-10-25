package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models;

import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ChampionshipEventTo;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails.ChampionshipSettingsTo;

import java.util.List;

public record CurrentChampionshipTo(
        String id,
        String clubId,
        String leaderboardID,
        String absoluteOpenDate,
        String absoluteCloseDate,
        ChampionshipSettingsTo settings,
        List<ChampionshipEventTo> events

) {
}
