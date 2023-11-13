package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails;

import lombok.Builder;

import java.util.List;

@Builder
public record ChampionshipTo(
        String id,
        String clubId,
        String leaderboardID,
        String absoluteOpenDate,
        String absoluteCloseDate,
        ChampionshipSettingsTo settings,
        List<ChampionshipEventTo> events

) {
}
