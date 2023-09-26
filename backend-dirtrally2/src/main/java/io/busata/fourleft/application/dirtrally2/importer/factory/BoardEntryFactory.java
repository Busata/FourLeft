package io.busata.fourleft.application.dirtrally2.importer.factory;

import io.busata.fourleft.domain.dirtrally2.players.PlayerInfo;
import io.busata.fourleft.domain.dirtrally2.players.PlayerInfoRepository;
import io.busata.fourleft.infrastructure.clients.racenet.dto.leaderboard.DR2LeaderboardEntry;
import io.busata.fourleft.domain.dirtrally2.clubs.BoardEntry;
import io.busata.fourleft.infrastructure.common.Factory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.transaction.Transactional;

@Factory
@Slf4j
@RequiredArgsConstructor
public class BoardEntryFactory {
    private final PlayerInfoRepository playerInfoRepository;

    @Transactional
    public BoardEntry create(DR2LeaderboardEntry result) {
        String name = result.name();
        PlayerInfo playerInfo = playerInfoRepository.findByRacenetOrAliases(name).orElseGet(() -> playerInfoRepository.save(new PlayerInfo(name)));

        BoardEntry entry = new BoardEntry();
        entry.setName(name);
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
