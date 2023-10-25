package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.clubDetails;

public record ChampionshipEventSettingsTo(
    Long vehicleClassID,
    String vehicleClass,
    Long weatherSeasonID,
    String weatherSeason,
    Long locationId,
    String location,
    String duration
) {
}
