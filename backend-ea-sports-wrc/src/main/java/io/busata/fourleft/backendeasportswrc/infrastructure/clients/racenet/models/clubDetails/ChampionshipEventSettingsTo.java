package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails;

import lombok.Builder;

@Builder
public record ChampionshipEventSettingsTo(
    Long vehicleClassID,
    String vehicleClass,
    Long weatherSeasonID,
    String weatherSeason,
    Long locationID,
    String location,
    String duration
) {
}
