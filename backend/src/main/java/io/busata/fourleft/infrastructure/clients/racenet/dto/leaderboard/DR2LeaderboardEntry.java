package io.busata.fourleft.infrastructure.clients.racenet.dto.leaderboard;

public record DR2LeaderboardEntry(
        long rank,
        String name,
        boolean isVIP,
        boolean isFounder,
        boolean isPlayer,
        boolean isDnfEntry,
        String vehicleName,
        String stageTime,
        String stageDiff,
        String totalTime,
        String totalDiff,
        String nationality
) {
}
