package io.busata.fourleft.api.easportswrc.models;

import io.busata.fourleft.common.ChannelClubMode;
import io.busata.fourleft.common.ScoringStrategy;

import java.util.List;
import java.util.Map;

public record ChannelConfigurationTo(
        String guildId,
        String channelId,
        boolean configured,
        // The primary club (first of clubs); kept so single-club clients keep working.
        String clubId,
        Boolean autopostingEnabled,
        Boolean requiresTracking,
        Boolean enabled,
        Boolean customScoringEnabled,
        Boolean timeTrialTopEnabled,
        Boolean timeTrialTopTrackedOnly,
        ScoringStrategy scoringStrategy,
        Map<String, Integer> scoringTable,
        ScoringAnchorsTo scoringAnchors,
        List<EventRestrictionTo> eventRestrictions,
        ChannelClubMode mode,
        List<ChannelClubTo> clubs,
        // What posting actually uses: MIXED only while the clubs are compatible, SINGLE otherwise.
        ChannelClubMode effectiveMode,
        // Null for a single-club channel.
        ChannelClubCompatibilityTo compatibility)
{
}
