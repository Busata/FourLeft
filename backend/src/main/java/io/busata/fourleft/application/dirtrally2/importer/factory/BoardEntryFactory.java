package io.busata.fourleft.application.dirtrally2.importer.factory;

import io.busata.fourleft.domain.dirtrally2.players.PlayerInfo;
import io.busata.fourleft.domain.dirtrally2.players.PlayerInfoRepository;
import io.busata.fourleft.infrastructure.clients.racenet.dto.leaderboard.DR2LeaderboardEntry;
import io.busata.fourleft.domain.dirtrally2.clubs.BoardEntry;
import io.busata.fourleft.infrastructure.common.Factory;
import lombok.RequiredArgsConstructor;

@Factory
@RequiredArgsConstructor
public class BoardEntryFactory {
    private final PlayerInfoRepository playerInfoRepository;

    public BoardEntry create(DR2LeaderboardEntry result) {

        PlayerInfo playerInfo = playerInfoRepository.findByRacenet(result.name()).orElseGet(() -> playerInfoRepository.save(new PlayerInfo(result.name())));

        BoardEntry entry = new BoardEntry();
        entry.setName(result.name());
        entry.setPlayerInfo(playerInfo);
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
