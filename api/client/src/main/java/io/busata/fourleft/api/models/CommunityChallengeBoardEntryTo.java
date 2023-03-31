package io.busata.fourleft.api.models;

public record CommunityChallengeBoardEntryTo(
        long rank,
        String name,
        String nationality,
        String vehicleName,
        String stageTime,
        String stageDiff,
        long totalRank,
        float percentageRank,
        boolean isDnf
) {
}
