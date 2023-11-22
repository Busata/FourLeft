package io.busata.fourleft.api.easportswrc.models;

public record DiscordClubCreateConfigurationTo(
        Long channelId,
        String clubId,
        boolean autoPosting
)
{
}
