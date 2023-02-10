package io.busata.fourleft.importer.updaters;


import io.busata.fourleft.domain.clubs.models.LeaderboardKey;
import io.busata.fourleft.domain.clubs.repository.LeaderboardRepository;
import io.busata.fourleft.gateway.racenet.RacenetGateway;
import io.busata.fourleft.gateway.racenet.dto.leaderboard.DR2LeaderboardRequest;
import io.busata.fourleft.gateway.racenet.dto.leaderboard.DR2LeaderboardResults;
import io.busata.fourleft.domain.clubs.models.BoardEntry;
import io.busata.fourleft.gateway.racenet.dto.leaderboard.PlatformFilter;
import io.busata.fourleft.gateway.racenet.factory.BoardEntryFactory;
import io.busata.fourleft.domain.clubs.models.Leaderboard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class LeaderboardFetcher {
    private final LeaderboardRepository leaderboardRepository;
    private final BoardEntryFactory boardEntryFactory;
    private final RacenetGateway client;

    public void upsertBoard(LeaderboardKey key) {
        upsertBoard(key.challengeId(), key.eventId(), String.valueOf(key.stageId()));
    }

    public void upsertBoard(String challengeId, String eventId, String stageId) {
        Leaderboard board = leaderboardRepository.findLeaderboardByChallengeIdAndEventIdAndStageId(challengeId, eventId, stageId)
                .orElseGet(Leaderboard::new);

        board.setChallengeId(challengeId);
        board.setEventId(eventId);
        board.setStageId(stageId);

        List<BoardEntry> entries = getBoardEntries(board);

        board.updateEntries(entries);
        leaderboardRepository.save(board);
    }

    private List<BoardEntry> getBoardEntries(Leaderboard board) {
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

    private DR2LeaderboardRequest buildLeaderboardRequest(String challengeId, String eventId, String stageId, int page) {
        return new DR2LeaderboardRequest(
                challengeId,
                eventId,
                "Unspecified",
                "Unspecified",
                "None",
                true,
                page,
                100,
                PlatformFilter.NONE.getFilterValue(),
                "Everyone",
                0,
                stageId);
    }

}
