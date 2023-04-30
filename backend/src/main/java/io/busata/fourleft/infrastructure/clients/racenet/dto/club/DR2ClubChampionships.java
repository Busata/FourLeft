package io.busata.fourleft.infrastructure.clients.racenet.dto.club;

import java.util.List;

public record DR2ClubChampionships(
        String id,
        String name,
        boolean isActive,
        boolean useHardcoreDamage,
        boolean useUnexpectedMoments,
        boolean forceCockpitCamera,
        boolean allowAssists,
        List<DR2ClubChampionshipEvent> events
) {
}
