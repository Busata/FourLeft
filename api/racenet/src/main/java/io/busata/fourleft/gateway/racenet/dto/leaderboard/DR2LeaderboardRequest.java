package io.busata.fourleft.gateway.racenet.dto.leaderboard;

public record DR2LeaderboardRequest(
        String challengeId,
        String eventId,
        String filterByAssists,
        String filterByWheel,
        String nationalityFilter,
        boolean orderByTotalTime,
        long page,
        long pageSize,
        String platformFilter,
        String playerFilter,
        long selectedEventId,
        String stageId
) {
}
