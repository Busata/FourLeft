package io.busata.fourleft.racenet.dto.club.championship.standings;

public record DR2ChampionshipStandingEntry (
        Long rank,
        String nationality,
        String displayName,
        Long totalPoints
) {
}
