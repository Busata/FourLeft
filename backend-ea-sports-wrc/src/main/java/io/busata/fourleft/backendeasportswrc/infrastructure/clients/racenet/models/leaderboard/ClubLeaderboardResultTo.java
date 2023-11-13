package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.leaderboard;

import lombok.Builder;

import java.util.List;

@Builder
public record ClubLeaderboardResultTo(
        String next,
        String previous,
        Long totalEntrantCount,
        List<ClubLeaderboardEntryTo> entries
) {
}
