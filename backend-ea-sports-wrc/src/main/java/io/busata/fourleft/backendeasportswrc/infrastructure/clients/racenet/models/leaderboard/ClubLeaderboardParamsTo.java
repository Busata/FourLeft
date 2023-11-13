package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.leaderboard;

public record ClubLeaderboardParamsTo(
        int maxResultCount,
        int platform,
        String cursor

) {
    public ClubLeaderboardParamsTo(String cursor) {
        this(10, 0, cursor);
    }
}
