package io.busata.fourleft.api.acrally.models;

import java.util.List;
import java.util.UUID;

/** One stage's leaderboard within an event, with the same labelling as the schedule view. */
public record StageBoardTo(
        UUID variantId,
        String label,
        String stageName,
        String locationName,
        List<LeaderboardEntryTo> entries) {
}
