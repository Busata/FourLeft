package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails;

public record ChampionshipEventStageTo(
        String id,
        String leaderboardID,
        ChampionshipEventStageSettingsTo stageSettings
) {
}
