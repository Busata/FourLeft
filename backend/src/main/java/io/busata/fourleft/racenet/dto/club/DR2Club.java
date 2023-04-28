package io.busata.fourleft.racenet.dto.club;

public record DR2Club(
        boolean hasFutureChampionships,
        Long id,
        String name,
        String description,
        long memberCount,
        long backgroundImageId,
        String clubAccessType,
        boolean isMember,
        boolean hasAskedToJoin,
        boolean hasBeenInvitedToJoin,
        boolean hasActiveChampionship,
        DR2ChampionshipProgress myChampionshipProgress
) {
}
