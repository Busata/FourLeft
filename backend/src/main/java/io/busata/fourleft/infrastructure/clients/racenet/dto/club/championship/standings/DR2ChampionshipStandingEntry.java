package io.busata.fourleft.infrastructure.clients.racenet.dto.club.championship.standings;

public record DR2ChampionshipStandingEntry (
        Long rank,
        String nationality,
        String displayName,
        Long totalPoints
) {
}
