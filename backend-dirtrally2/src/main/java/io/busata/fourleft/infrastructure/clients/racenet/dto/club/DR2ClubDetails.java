package io.busata.fourleft.infrastructure.clients.racenet.dto.club;

public record DR2ClubDetails(
        String result,
        DR2Club club,
        long pendingInvites,
        String role
) {
}
