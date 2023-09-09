package io.busata.fourleft.application.dirtrally2.importer;


import io.busata.fourleft.domain.dirtrally2.clubs.PlatformInfo;
import io.busata.fourleft.domain.dirtrally2.clubs.LeaderboardRepository;
import io.busata.fourleft.common.ControllerType;
import io.busata.fourleft.common.Platform;
import io.busata.fourleft.domain.dirtrally2.players.PlayerInfo;
import io.busata.fourleft.domain.dirtrally2.players.PlayerInfoRepository;
import io.busata.fourleft.infrastructure.clients.racenet.RacenetGateway;
import io.busata.fourleft.infrastructure.clients.racenet.dto.leaderboard.ControllerFilter;
import io.busata.fourleft.infrastructure.clients.racenet.dto.leaderboard.DR2LeaderboardRequest;
import io.busata.fourleft.infrastructure.clients.racenet.dto.leaderboard.DR2LeaderboardResults;
import io.busata.fourleft.domain.dirtrally2.clubs.BoardEntry;
import io.busata.fourleft.infrastructure.clients.racenet.dto.leaderboard.PlatformFilter;
import io.busata.fourleft.application.dirtrally2.importer.factory.BoardEntryFactory;
import io.busata.fourleft.domain.dirtrally2.clubs.Leaderboard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class LeaderboardFetcher {
    private final LeaderboardRepository leaderboardRepository;
    private final BoardEntryFactory boardEntryFactory;
    private final PlayerInfoRepository playerInfoRepository;
    private final RacenetGateway client;

    public void upsertBoard(String challengeId, String eventId, String stageId, boolean syncPlatform) {
        Leaderboard board = leaderboardRepository.findLeaderboardByChallengeIdAndEventIdAndStageId(challengeId, eventId, stageId)
                .orElseGet(Leaderboard::new);

        board.setChallengeId(challengeId);
        board.setEventId(eventId);
        board.setStageId(stageId);

        List<BoardEntry> entries = getBoardEntries(board);

        if (syncPlatform) {
            updatePlatform(board, entries);
        }

        board.updateEntries(entries);
        leaderboardRepository.save(board);
    }

    private void updatePlatform(Leaderboard board, List<BoardEntry> entries) {
        log.info("Updating platform");

        List<String> names = entries.stream().map(BoardEntry::getName).collect(Collectors.toList());

        List<PlayerInfo> unsyncedEntries = names.stream().flatMap(name -> playerInfoRepository.findByRacenet(name).stream())
                .filter(playerInfo -> !playerInfo.isSyncedPlatform())
                .toList();

        if (unsyncedEntries.isEmpty()) {
            return;
        }

        HashMap<String, PlatformInfo> platformInfoPerPlayer = getPlatformInfo(board);

        final var updatedPlayerInfos = unsyncedEntries
                .stream()
                .flatMap(playerInfo ->
                        playerInfo.getRacenets().stream()
                                .filter(platformInfoPerPlayer::containsKey)
                                .findFirst()
                                .map(platformInfoPerPlayer::get)
                                .map(platformInfo -> {
                                    playerInfo.setPlatform(platformInfo.getPlatform());
                                    playerInfo.setController(platformInfo.getControllerType());
                                    playerInfo.setSyncedPlatform(true);
                                    return playerInfo;
                }).stream()).toList();

        log.info("Synced platforms for {} players.", updatedPlayerInfos.size());

        playerInfoRepository.saveAll(updatedPlayerInfos);
    }

    public List<BoardEntry> getBoardEntries(Leaderboard board) {
        boolean done = false;
        int page = 1;

        List<BoardEntry> entries = new ArrayList<>();

        while (!done) {
            log.info("-- -- Fetching page {}", page);
            DR2LeaderboardRequest request = buildLeaderboardRequest(
                    board.getChallengeId(),
                    board.getEventId(),
                    String.valueOf(board.getStageId()),
                    page);
            DR2LeaderboardResults leaderboard = client.getLeaderboard(request);

            log.info("-- -- -- Creating entries...");

            List<BoardEntry> pageEntries = leaderboard.entries().stream().map(boardEntryFactory::create)
                    .peek(entry -> entry.setLeaderboard(board))
                    .collect(Collectors.toList());
            entries.addAll(pageEntries);

            if (page >= leaderboard.pageCount()) {
                done = true;
            } else {
                page += 1;
            }
        }
        return entries;
    }

    public HashMap<String, PlatformInfo> getPlatformInfo(Leaderboard board) {

        HashMap<String, PlatformInfo> platformInfo = new HashMap<>();

        List.of(PlatformFilter.STEAM, PlatformFilter.PLAYSTATION, PlatformFilter.XBOX).forEach(platformFilter -> {

            boolean done = false;
            int page = 1;

            log.info("-- Getting board for filter {}", platformFilter);

            while (!done) {
                log.info("-- -- Fetching page {}", page);
                DR2LeaderboardRequest request = buildLeaderboardRequest(
                        board.getChallengeId(),
                        board.getEventId(),
                        String.valueOf(board.getStageId()),
                        platformFilter,
                        ControllerFilter.ALL,
                        page);
                DR2LeaderboardResults leaderboard = client.getLeaderboard(request);

                leaderboard.entries().forEach(entry -> {
                    platformInfo.putIfAbsent(entry.name(), new PlatformInfo(entry.name()));

                    platformInfo.computeIfPresent(entry.name(), (key, value) -> {

                        value.setPlatform(createPlatformFromFilter(platformFilter));

                        return value;
                    });


                });

                if (page >= leaderboard.pageCount()) {
                    done = true;
                } else {
                    page += 1;
                }
            }
        });

        List.of(ControllerFilter.WHEEL, ControllerFilter.CONTROLLER).forEach(controllerFilter -> {

            boolean done = false;
            int page = 1;

            log.info("-- Getting board for filter {}", controllerFilter);

            while (!done) {
                log.info("-- -- Fetching page {}", page);
                DR2LeaderboardRequest request = buildLeaderboardRequest(
                        board.getChallengeId(),
                        board.getEventId(),
                        String.valueOf(board.getStageId()),
                        PlatformFilter.NONE,
                        controllerFilter,
                        page);
                DR2LeaderboardResults leaderboard = client.getLeaderboard(request);

                leaderboard.entries().forEach(entry -> {
                    platformInfo.putIfAbsent(entry.name(), new PlatformInfo(entry.name()));

                    platformInfo.computeIfPresent(entry.name(), (key, value) -> {

                        value.setControllerType(createControllerTypeFromFilter(controllerFilter));

                        return value;
                    });


                });

                if (page >= leaderboard.pageCount()) {
                    done = true;
                } else {
                    page += 1;
                }
            }
        });

        return platformInfo;
    }

    private ControllerType createControllerTypeFromFilter(ControllerFilter controllerFilter) {
        return switch (controllerFilter) {
            case ALL -> ControllerType.UNKNOWN;
            case CONTROLLER -> ControllerType.CONTROLLER;
            case WHEEL -> ControllerType.WHEEL;
        };
    }

    private Platform createPlatformFromFilter(PlatformFilter platformFilter) {
        return switch (platformFilter) {

            case NONE -> Platform.UNKNOWN;
            case STEAM -> Platform.PC;
            case PLAYSTATION -> Platform.PLAYSTATION;
            case XBOX -> Platform.XBOX;
        };
    }

    private DR2LeaderboardRequest buildLeaderboardRequest(
            String challengeId,
            String eventId,
            String stageId,
            int page) {

        return buildLeaderboardRequest(challengeId, eventId, stageId, PlatformFilter.NONE, ControllerFilter.ALL, page);
    }

    private DR2LeaderboardRequest buildLeaderboardRequest(
            String challengeId,
            String eventId,
            String stageId,
            PlatformFilter platformFilter,
            ControllerFilter controllerFilter,
            int page) {
        return new DR2LeaderboardRequest(
                challengeId,
                eventId,
                "Unspecified",
                controllerFilter.getValue(),
                "None",
                true,
                page,
                50,
                platformFilter.getValue(),
                "Everyone",
                0,
                stageId);
    }

}
