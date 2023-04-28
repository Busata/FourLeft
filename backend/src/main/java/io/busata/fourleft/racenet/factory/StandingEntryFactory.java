package io.busata.fourleft.racenet.factory;

import io.busata.fourleft.racenet.dto.club.championship.standings.DR2ChampionshipStandingEntry;
import io.busata.fourleft.domain.clubs.models.StandingEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
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
