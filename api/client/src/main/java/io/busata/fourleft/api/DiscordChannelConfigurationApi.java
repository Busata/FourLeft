package io.busata.fourleft.api;

import io.busata.fourleft.api.models.configuration.create.DiscordChannelConfigurationTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DiscordChannelConfigurationApi {

    @GetMapping(Routes.ALL_DISCORD_CHANNEL_CONFIGURATION)
    List<DiscordChannelConfigurationTo> getConfigurations();

    @PostMapping(Routes.DISCORD_CHANNEL_CONFIGURATION)
    UUID createConfiguration(@PathVariable Long channelId, @RequestBody DiscordChannelConfigurationTo clubViewTo);

    @GetMapping(Routes.DISCORD_CHANNEL_CONFIGURATION)
    Optional<DiscordChannelConfigurationTo> getConfiguration(@PathVariable Long channelId);
}
