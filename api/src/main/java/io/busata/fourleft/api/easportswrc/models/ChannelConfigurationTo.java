package io.busata.fourleft.api.easportswrc.models;

public record ChannelConfigurationTo(
        String guildId,
        String channelId,
        boolean configured,
        String clubId,
        Boolean autopostingEnabled,
        Boolean requiresTracking,
        Boolean enabled)
{
}
