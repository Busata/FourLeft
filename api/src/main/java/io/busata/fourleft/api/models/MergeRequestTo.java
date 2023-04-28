package io.busata.fourleft.api.models;

import io.busata.fourleft.domain.clubs.models.LeaderboardKey;

public record MergeRequestTo(
        LeaderboardKey firstClub,
        LeaderboardKey secondClub
) {
}
