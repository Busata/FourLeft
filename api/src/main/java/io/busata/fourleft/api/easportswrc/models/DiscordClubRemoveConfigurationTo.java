package io.busata.fourleft.api.easportswrc.models;

public record DiscordClubRemoveConfigurationTo(
        Long guildId,
        Long channelId,
        String clubId
)
{
}
