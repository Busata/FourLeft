package io.busata.fourleft.api.easportswrc.models;

import io.busata.fourleft.common.ChannelClubMode;
import io.busata.fourleft.common.ScoringStrategy;

import java.util.List;
import java.util.Map;

public record ChannelConfigurationUpdateTo(
        boolean autopostingEnabled,
        boolean requiresTracking,
        boolean customScoringEnabled,
        boolean timeTrialTopEnabled,
        boolean timeTrialTopTrackedOnly,
        ScoringStrategy scoringStrategy,
        Map<String, Integer> scoringTable,
        ScoringAnchorsTo scoringAnchors,
        List<EventRestrictionTo> eventRestrictions,
        // Null keeps the current mode.
        ChannelClubMode mode)
{
}
