package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails;

import lombok.Builder;

import java.util.List;

@Builder
public record ChampionshipEventTo(
        String id,
        String leaderboardID,
        String absoluteOpenDate,
        String absoluteCloseDate,
        Long status,
        ChampionshipEventSettingsTo eventSettings,
        List<ChampionshipEventStageTo> stages
) {
}
