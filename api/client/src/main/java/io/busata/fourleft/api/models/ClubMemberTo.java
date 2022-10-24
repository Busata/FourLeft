package io.busata.fourleft.api.models;

public record ClubMemberTo(
        String displayName,
        String membershipType,
        long championshipGolds,
        long championshipSilvers,
        long championshipBronzes,
        long championshipParticipation
) {
}
