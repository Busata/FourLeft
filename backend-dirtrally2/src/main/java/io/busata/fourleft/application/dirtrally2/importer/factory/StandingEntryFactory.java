package io.busata.fourleft.application.dirtrally2.importer.factory;

import io.busata.fourleft.infrastructure.clients.racenet.dto.club.championship.standings.DR2ChampionshipStandingEntry;
import io.busata.fourleft.domain.dirtrally2.clubs.StandingEntry;
import io.busata.fourleft.infrastructure.common.Factory;
import lombok.RequiredArgsConstructor;

@Factory
@RequiredArgsConstructor
public class StandingEntryFactory {

    public StandingEntry create(DR2ChampionshipStandingEntry dr2Standing) {
        StandingEntry entry = new StandingEntry();
        entry.setDisplayName(dr2Standing.displayName());
        entry.setNationality(dr2Standing.nationality());
        entry.setTotalPoints(dr2Standing.totalPoints());
        entry.setRank(dr2Standing.rank());
        return entry;

    }
}
