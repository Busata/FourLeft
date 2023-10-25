package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models;

public record EAWRCClubTo(
    long clubId,
    String clubName,
    long likeCount,
    long dislikeCount
) {
}
