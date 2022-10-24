package io.busata.fourleft.api.models;

public record CustomChampionshipStandingEntryTo(
        Long rank,
        String nationality,
        String displayName,
        Long points,
        Long powerStagePoints
) {
}
