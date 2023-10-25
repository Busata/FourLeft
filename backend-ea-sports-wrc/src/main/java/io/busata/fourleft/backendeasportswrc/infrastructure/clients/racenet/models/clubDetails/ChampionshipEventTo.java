package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails;

import java.util.List;

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
