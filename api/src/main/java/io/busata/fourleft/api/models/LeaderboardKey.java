package io.busata.fourleft.api.models;

public record LeaderboardKey(
        String challengeId,
        String eventId,
        String stageId
) {
}
