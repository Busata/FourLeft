package io.busata.fourleft.infrastructure.clients.racenet.dto.club.championship.creation;

public record DR2ChampionshipCreateStage(
        String route,
        String serviceArea, //eMedium,
        long surfaceDegradation, // 0 25 100
        String timeOfDay
) {
}
