package io.busata.fourleft.api.models;

public record ChampionshipStageSummaryTo(String name,
                                         boolean isHardcoreDamage,
                                         boolean useAssists,
                                         boolean unexpectedMoments,
                                         boolean forceCockpit,
                                         ChampionshipStageEntryTo stageSummary) {
}
