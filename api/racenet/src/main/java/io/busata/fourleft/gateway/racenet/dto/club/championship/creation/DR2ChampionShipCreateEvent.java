package io.busata.fourleft.gateway.racenet.dto.club.championship.creation;


import java.util.List;

public record DR2ChampionShipCreateEvent(
        String disciplineId, //eRally,
        long durationDays,
        long durationHours,
        long durationMins,
        String locationId,
        String vehicleClassRestriction,
        List<DR2ChampionshipCreateStage> stages
) {
}