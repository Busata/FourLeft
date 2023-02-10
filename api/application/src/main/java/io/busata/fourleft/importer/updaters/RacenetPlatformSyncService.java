package io.busata.fourleft.importer.updaters;

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
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class RacenetPlatformSyncService {

    private final PlayerInfoRepository playerInfoRepository;
    private final BoardEntryRepository boardEntryRepository;

    private final LeaderboardFetcher leaderboardFetcher;

    private final LeaderboardRepository leaderboardRepository;

    @Transactional
    public void syncPlayers(long participations) {
        findNewPlayers();

        List<PlayerInfo> bySyncedPlatformIsFalse = playerInfoRepository.findBySyncedPlatformIsFalse();
        log.info("Platform sync - unsynced players: {}", bySyncedPlatformIsFalse.size());

        List<PlayerInfo> bySyncedPlatformIsTrue = playerInfoRepository.findBySyncedPlatformIsTrue();
        log.info("Platform sync - synced players: {}", bySyncedPlatformIsTrue.size());

        List<UUID> frequentParticipants = boardEntryRepository.findUnsyncedLeaderboardsByParticipations(participations);

        log.info("Unsynced (participations: {}): {}", frequentParticipants.size());

        frequentParticipants.stream().limit(1).forEach(leaderboardId -> {
            log.info("Syncing board: {}", leaderboardId);
            Leaderboard leaderboard = leaderboardRepository.getById(leaderboardId);

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


            List<String> strings = boardEntryRepository.findUnsyncedNamesForLeaderboard(leaderboardId).stream().toList();
            List<PlayerInfo> outdatedPlayerInfos = playerInfoRepository.findByRacenetIn(strings).stream().map(playerInfo -> {

                playerInfo.setSyncedPlatform(true);
                playerInfo.setOutdated(true);
                return playerInfo;
            }).toList();

            playerInfoRepository.saveAll(outdatedPlayerInfos);
        });

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
