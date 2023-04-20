package io.busata.fourleft.endpoints.discord.configuration;

import io.busata.fourleft.api.DiscordChannelConfigurationApi;
import io.busata.fourleft.api.models.configuration.create.DiscordChannelConfigurationTo;
import io.busata.fourleft.domain.configuration.DiscordChannelConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@RestController
@RequiredArgsConstructor
public class DiscordChannelConfigurationEndpoint implements DiscordChannelConfigurationApi {
    private final DiscordChannelConfigurationFactory discordChannelConfigurationFactory;
    private final DiscordChannelConfigurationToFactory discordChannelConfigurationToFactory;
    private final DiscordChannelConfigurationService discordChannelConfigurationService;


    @Override
    public List<DiscordChannelConfigurationTo> getConfigurations() {
        return this.discordChannelConfigurationService.findAll().stream().map(discordChannelConfigurationToFactory::create).collect(Collectors.toList());

    }

    @Override
    public UUID createConfiguration(@PathVariable Long channelId, @RequestBody DiscordChannelConfigurationTo clubViewTo) {
        DiscordChannelConfiguration configuration = discordChannelConfigurationFactory.create(channelId, clubViewTo);
        return discordChannelConfigurationService.createConfiguration(configuration);
    }

    @Override
    public Optional<DiscordChannelConfigurationTo> getConfiguration(@PathVariable Long channelId) {
        return this.discordChannelConfigurationService.findConfigurationByChannelId(channelId)
                .map(discordChannelConfigurationToFactory::create);

    }

    @Override
    public void deleteConfiguration(@PathVariable UUID configurationId) {
        this.discordChannelConfigurationService.deleteConfigurationByChannelId(configurationId);
    }
}
