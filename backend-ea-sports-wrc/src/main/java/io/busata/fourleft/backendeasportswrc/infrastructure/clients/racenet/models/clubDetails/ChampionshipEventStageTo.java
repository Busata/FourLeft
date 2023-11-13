package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails;

import lombok.Builder;

@Builder
public record ChampionshipEventStageTo(
        String id,
        String leaderboardID,
        ChampionshipEventStageSettingsTo stageSettings
) {
}
