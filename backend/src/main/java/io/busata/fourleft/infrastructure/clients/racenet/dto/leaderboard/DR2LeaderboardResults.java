package io.busata.fourleft.infrastructure.clients.racenet.dto.leaderboard;


import java.util.List;

public record DR2LeaderboardResults(
        long pageRequested,
        long pageSize,
        long pageCount,
        List<DR2LeaderboardEntry> entries
) {

}
