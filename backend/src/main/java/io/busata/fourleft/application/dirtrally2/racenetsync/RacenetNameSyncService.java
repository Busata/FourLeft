package io.busata.fourleft.application.dirtrally2.racenetsync;

import io.busata.fourleft.application.dirtrally2.importer.LeaderboardFetcher;
import io.busata.fourleft.domain.dirtrally2.clubs.BoardEntry;
import io.busata.fourleft.domain.dirtrally2.clubs.Leaderboard;
import io.busata.fourleft.domain.dirtrally2.clubs.LeaderboardRepository;
import io.busata.fourleft.domain.dirtrally2.players.PlayerInfo;
import io.busata.fourleft.domain.dirtrally2.players.PlayerInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RacenetNameSyncService {

    private final LeaderboardRepository leaderboardRepository;
    private final LeaderboardFetcher leaderboardFetcher;
    private final PlayerInfoRepository playerInfoRepository;


    public Optional<Leaderboard> findMostCommonBoard() {
        Map<UUID, Integer> boardByCount = new HashMap<>();

        List<String> unsyncedPlayers = playerInfoRepository.findByCreatedBeforeRacenetChangeIsTrue().stream().map(PlayerInfo::getRacenet).limit(500).toList();

        for (int i = 0; i < unsyncedPlayers.size(); i++) {
            log.info("Checking player {}/{}", i + 1, unsyncedPlayers.size());
            String player = unsyncedPlayers.get(i);

            List<Leaderboard> leaderboardsWhereRacenetPresent = leaderboardRepository.findLeaderboardsWhereRacenetPresent(player);

            leaderboardsWhereRacenetPresent.forEach(board -> {
                boardByCount.put(board.getId(), boardByCount.getOrDefault(board.getId(), 0) + 1);
            });
        }

        if (boardByCount.isEmpty()) {
            return Optional.empty();
        }

        UUID key = Collections.max(boardByCount.entrySet(), Map.Entry.comparingByValue()).getKey();

        return leaderboardRepository.findById(key);
    }

    @Transactional
    public void sync() {
        log.info("Searching most common board");
        findMostCommonBoard().ifPresentOrElse(board -> {
            log.info("Comparing entries");
            final var oldEntries = board.getEntries();
            final var newEntries = leaderboardFetcher.getBoardEntries(board);

            if (oldEntries.size() != newEntries.size()) {
                log.warn("Board entries don't match, still in progress?");
                return;
            }

            oldEntries.sort(Comparator.comparing(BoardEntry::getRank));
            newEntries.sort(Comparator.comparing(BoardEntry::getRank));

            List<PlayerInfo> infos = new ArrayList<>();

            for (int i = 0; i < oldEntries.size(); i++) {
                var oldEntry = oldEntries.get(i);
                var newEntry = newEntries.get(i);

                if (!oldEntry.getName().equals(newEntry.getName())) {
                    log.info("-- Update names for {} to {}", oldEntry.getName(), newEntry.getName());
                }

                playerInfoRepository.findByRacenetAndCreatedBeforeRacenetChangeIsTrue(oldEntry.getName()).ifPresent(playerInfo -> {
                    playerInfo.setPlatformName(newEntry.getName());
                    playerInfo.setCreatedBeforeRacenetChange(false);
                    infos.add(playerInfo);
                });
            }

            playerInfoRepository.saveAll(infos);
            infos.clear();
        }, () -> {
            log.info("No boards found");
        });
    }

}
