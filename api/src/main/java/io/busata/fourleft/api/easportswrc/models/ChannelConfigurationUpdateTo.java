package io.busata.fourleft.api.easportswrc.models;

import io.busata.fourleft.common.ScoringStrategy;

import java.util.List;
import java.util.Map;

public record ChannelConfigurationUpdateTo(
        boolean autopostingEnabled,
        boolean requiresTracking,
        boolean customScoringEnabled,
        ScoringStrategy scoringStrategy,
        Map<String, Integer> scoringTable,
        ScoringAnchorsTo scoringAnchors,
        List<EventRestrictionTo> eventRestrictions)
{
}
