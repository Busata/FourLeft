package io.busata.fourleft.endpoints.configuration;

import io.busata.fourleft.api.Routes;
import io.busata.fourleft.api.models.configuration.DiscordChannelConfigurationTo;
import io.busata.fourleft.api.models.configuration.create.CreateDiscordChannelConfigurationTo;
import io.busata.fourleft.domain.configuration.DiscordChannelConfiguration;
import io.busata.fourleft.domain.configuration.DiscordChannelConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

import static java.util.List.of;

@RestController
@RequiredArgsConstructor
public class DiscordChannelConfigurationEndpoint {
    private final DiscordChannelConfigurationFactory discordChannelConfigurationFactory;
    private final DiscordChannelConfigurationToFactory discordChannelConfigurationToFactory;
    private final DiscordChannelConfigurationService discordChannelConfigurationService;

    @PostMapping(Routes.DISCORD_CHANNEL_CONFIGURATION)
    public UUID createConfiguration(@PathVariable Long channelId, @RequestBody CreateDiscordChannelConfigurationTo clubViewTo) {
        DiscordChannelConfiguration configuration = discordChannelConfigurationFactory.create(channelId, clubViewTo);
        return discordChannelConfigurationService.createConfiguration(configuration);
    }

    @GetMapping(Routes.DISCORD_CHANNEL_CONFIGURATION)
    public Optional<CreateDiscordChannelConfigurationTo> getConfiguration(@PathVariable Long channelId) {

        return this.discordChannelConfigurationService.findConfigurationByChannelId(channelId)
                .map(discordChannelConfigurationToFactory::create);

    }
}
