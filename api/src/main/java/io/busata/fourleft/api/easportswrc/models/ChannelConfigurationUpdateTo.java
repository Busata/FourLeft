package io.busata.fourleft.api.easportswrc.models;

public record ChannelConfigurationUpdateTo(
        boolean autopostingEnabled,
        boolean requiresTracking)
{
}
