package io.busata.fourleft.infrastructure.clients.racenet.dto.club;

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
