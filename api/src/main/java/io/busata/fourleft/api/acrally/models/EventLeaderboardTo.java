package io.busata.fourleft.api.acrally.models;

import java.util.List;
import java.util.UUID;

/** An event's full leaderboard: per-stage boards plus the derived overall standings. */
public record EventLeaderboardTo(
        UUID eventId,
        String label,
        int totalStages,
        List<StageBoardTo> stages,
        List<EventStandingTo> overall) {
}
