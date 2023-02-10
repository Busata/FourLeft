package io.busata.fourleft.importer.updaters;

import io.busata.fourleft.domain.clubs.models.BoardEntry;
import io.busata.fourleft.domain.clubs.models.Leaderboard;
import io.busata.fourleft.domain.clubs.models.PlatformInfo;
import io.busata.fourleft.domain.clubs.repository.BoardEntryRepository;
import io.busata.fourleft.domain.clubs.repository.LeaderboardRepository;
import io.busata.fourleft.domain.players.PlayerInfo;
import io.busata.fourleft.domain.players.PlayerInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class RacenetPlatformSyncService {

    private final PlayerInfoRepository playerInfoRepository;
    private final BoardEntryRepository boardEntryRepository;

    private final LeaderboardFetcher leaderboardFetcher;


    @Transactional
    public void syncPlayers(String racenet) {
        findNewPlayers();

        log.info("Syncing platforms for player {}", racenet);

        List<BoardEntry> entries = boardEntryRepository.findByName(racenet);

        Leaderboard leaderboard = entries.get(0).getLeaderboard();

        HashMap<String, PlatformInfo> platformInfoPerPlayer = leaderboardFetcher.getPlatformInfo(leaderboard);

        log.info("Saving data");

        List<PlayerInfo> playerInfos = playerInfoRepository.findByRacenetIn(platformInfoPerPlayer.keySet().stream().toList()).stream().map(playerInfo -> {

            final var platformInfo = platformInfoPerPlayer.get(playerInfo.getRacenet());

            playerInfo.setPlatform(platformInfo.getPlatform());
            playerInfo.setController(platformInfo.getControllerType());
            playerInfo.setSyncedPlatform(true);
            return playerInfo;
        }).toList();

        playerInfoRepository.saveAll(playerInfos);

        log.info("Sync done");

    }

    private void findNewPlayers() {
        List<PlayerInfo> players = boardEntryRepository.findNamesWithoutPlayerInfo().stream()
                .map(PlayerInfo::new)
                .toList();

        log.info("Platform sync - new players: {}", players.size());

        playerInfoRepository.saveAll(players);
    }
}
