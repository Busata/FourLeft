package io.busata.fourleft.gateway.racenet.dto.club.championship.standings;

import java.util.List;

public record DR2ChampionshipStandings(
        long pageCount,
        long eventCount,
        List<DR2ChampionshipStandingEntry> standings
) {
}
