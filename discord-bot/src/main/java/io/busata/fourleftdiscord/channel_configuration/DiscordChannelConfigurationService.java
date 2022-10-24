package io.busata.fourleftdiscord.channel_configuration;

import discord4j.common.util.Snowflake;
import io.busata.fourleft.api.models.configuration.DiscordChannelConfigurationTo;
import io.busata.fourleftdiscord.client.FourLeftClient;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DiscordChannelConfigurationService {
    private final FourLeftClient api;

    @Cacheable("channel_configurations")
    public List<DiscordChannelConfigurationTo> getConfigurations() {
        return this.api.getDiscordChannelConfigurations();
    }


    public DiscordChannelConfigurationTo findConfigurationByChannelId(Long channelId) {
        return this.getConfigurations().stream().filter(channelConfigurationTo -> channelConfigurationTo.channelId().equals(channelId)).findFirst().orElseThrow();
    }

    public DiscordChannelConfigurationTo findConfigurationByChannelId(Snowflake channelId) {
        return findConfigurationByChannelId(channelId.asLong());
    }

    public UUID getViewId(Snowflake channelId) {
        final var configuration = this.findConfigurationByChannelId(channelId);
        return configuration.clubViewTo().id();
    }
}
