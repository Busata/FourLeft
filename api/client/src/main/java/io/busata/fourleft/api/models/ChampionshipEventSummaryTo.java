package io.busata.fourleft.api.models;

import java.util.List;

public record ChampionshipEventSummaryTo(
        String name,
        boolean isHardcoreDamage,
        boolean useAssists,
        boolean unexpectedMoments,
        boolean forceCockpit,
        List<ChampionshipEventEntryTo> events
                                         ) {
}
