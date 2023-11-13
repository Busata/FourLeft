package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.leaderboard;

import lombok.Builder;

@Builder
public record ClubLeaderboardEntryTo(
        String ssid,
        String displayName,
        Long rank,
        String leaderboardId,
        String wrcPlayerId,
        String time,
        String differenceToFirst,
        Long nationalityID,
        String timeAccumulated,
        Long platform,
        String vehicle,
        String timePenalty
) {
}
