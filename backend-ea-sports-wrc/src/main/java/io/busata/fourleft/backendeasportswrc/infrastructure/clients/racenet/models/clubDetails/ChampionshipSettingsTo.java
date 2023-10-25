package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails;

public record ChampionshipSettingsTo(
        String name,
        Long format,
        Long bonusPointsMode,
        Long scoringSystem,
        Long trackDegradation,
        Long isHardcoreDamageEnabled,
        Long isAssistsAllowed,
        Long isTuningAllowed
) {
}
