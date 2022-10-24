package io.busata.fourleft.gateway.racenet.factory;

import io.busata.fourleft.gateway.racenet.dto.leaderboard.DR2LeaderboardEntry;
import io.busata.fourleft.domain.clubs.models.BoardEntry;
import org.springframework.stereotype.Component;

@Component
public class BoardEntryFactory {

    public BoardEntry create(DR2LeaderboardEntry result) {
        BoardEntry entry = new BoardEntry();
        entry.setName(result.name());
        entry.setRank(result.rank());
        entry.setNationality(result.nationality());
        entry.setDnf(result.isDnfEntry());
        entry.setVehicleName(result.vehicleName());
        entry.setStageTime(result.stageTime());
        entry.setStageDiff(result.stageDiff());
        entry.setTotalDiff(result.totalDiff());
        entry.setTotalTime(result.totalTime());
        return entry;
    }
}
