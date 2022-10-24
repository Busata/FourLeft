package io.busata.fourleft.gateway.racenet.dto.club;

public record DR2ClubDetails(
        String result,
        DR2Club club,
        long pendingInvites,
        String role
) {
}
