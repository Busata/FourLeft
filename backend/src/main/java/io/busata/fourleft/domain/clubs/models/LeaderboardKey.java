package io.busata.fourleft.domain.clubs.models;

public record LeaderboardKey(
        String challengeId,
        String eventId,
        String stageId
) {
}
