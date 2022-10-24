package io.busata.fourleft.gateway.racenet.dto.club;

public record DR2ClubMember(
        String id,
        String displayName,
        String membershipType,
        long championshipGolds,
        long championshipSilvers,
        long championshipBronzes,
        long championshipParticipation
) {
}
