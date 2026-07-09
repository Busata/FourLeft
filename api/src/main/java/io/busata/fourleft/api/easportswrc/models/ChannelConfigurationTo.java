package io.busata.fourleft.api.easportswrc.models;

import io.busata.fourleft.common.ScoringStrategy;

import java.util.List;
import java.util.Map;

public record ChannelConfigurationTo(
        String guildId,
        String channelId,
        boolean configured,
        String clubId,
        Boolean autopostingEnabled,
        Boolean requiresTracking,
        Boolean enabled,
        Boolean customScoringEnabled,
        ScoringStrategy scoringStrategy,
        Map<String, Integer> scoringTable,
        ScoringAnchorsTo scoringAnchors,
        List<EventRestrictionTo> eventRestrictions)
{
}
