package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails;

import lombok.Builder;

@Builder
public record ChampionshipSettingsTo(
        String name,
        Long format,
        Long bonusPointsMode,
        Long scoringSystem,
        Long trackDegradation,
        Boolean isHardcoreDamageEnabled,
        Boolean isAssistsAllowed,
        Boolean isTuningAllowed
) {
}
