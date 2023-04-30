package io.busata.fourleft.application.dirtrally2.importer.racenet;

import io.busata.fourleft.infrastructure.clients.racenet.dto.leaderboard.DR2LeaderboardEntry;
import io.busata.fourleft.domain.dirtrally2.clubs.BoardEntry;
import io.busata.fourleft.infrastructure.common.Factory;

@Factory
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
