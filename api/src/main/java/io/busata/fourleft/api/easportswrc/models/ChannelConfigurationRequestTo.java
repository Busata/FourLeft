package io.busata.fourleft.api.easportswrc.models;

public record ChannelConfigurationRequestTo(
        Long guildId,
        Long channelId,
        String discordId)
{
}
