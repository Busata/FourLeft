package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.leaderboard;

public record ClubStandingsParamsTo(
        int resultCount,
        String cursor

) {
    public ClubStandingsParamsTo(String cursor) {
        this(10, cursor);
    }
}
