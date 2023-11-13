package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails;

import lombok.Builder;

@Builder
public record ChampionshipEventStageSettingsTo(
        Long routeID,
        String route,
        Long weatherAndSurfaceID,
        String weatherAndSurface,
        Long timeOfDayID,
        String timeOfDay,
        Long serviceAreaID,
        String serviceArea
) {
}
