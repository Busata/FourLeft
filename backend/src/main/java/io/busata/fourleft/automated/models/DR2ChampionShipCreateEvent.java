package io.busata.fourleft.automated.models;


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