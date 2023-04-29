package io.busata.fourleft.infrastructure.clients.racenet.dto.club;

public record DR2ClubChampionshipEvent(
        String id,
        String countryId,
        String countryName,
        String locationId,
        String locationName,
        String firstStageRouteId,
        String firstStageConditions,
        boolean hasParticipated,
        String eventStatus,
        String eventTime,
        boolean isDlc,
        String vehicleClass,
        DR2ClubEntryWindow entryWindow
) {
}
