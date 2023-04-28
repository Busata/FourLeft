package io.busata.fourleft.api.models;

public record ChampionshipStandingEntryTo(
        Long rank,
        String nationality,
        String displayName,
        Long points
) {
}
