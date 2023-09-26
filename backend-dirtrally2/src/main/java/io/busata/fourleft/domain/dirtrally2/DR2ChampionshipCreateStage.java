package io.busata.fourleft.domain.dirtrally2;

public record DR2ChampionshipCreateStage(
        String route,
        String serviceArea, //eMedium,
        long surfaceDegradation, // 0 25 100
        String timeOfDay
) {
}
