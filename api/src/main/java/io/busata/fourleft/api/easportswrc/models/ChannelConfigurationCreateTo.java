package io.busata.fourleft.api.easportswrc.models;

public record ChannelConfigurationCreateTo(
        String clubId,
        boolean autopostingEnabled,
        boolean requiresTracking)
{
}
