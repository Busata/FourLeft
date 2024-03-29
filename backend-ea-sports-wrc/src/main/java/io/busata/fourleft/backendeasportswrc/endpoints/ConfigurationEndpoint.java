package io.busata.fourleft.backendeasportswrc.endpoints;

import io.busata.fourleft.api.easportswrc.events.ConfigurationUpdatedEvent;
import io.busata.fourleft.api.easportswrc.models.DiscordClubConfigurationTo;
import io.busata.fourleft.api.easportswrc.models.DiscordClubCreateConfigurationTo;
import io.busata.fourleft.api.easportswrc.models.DiscordClubRemoveConfigurationTo;
import io.busata.fourleft.backendeasportswrc.application.discord.configuration.DiscordClubConfigurationService;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.DiscordGateway;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.models.SimpleDiscordMessageTo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ConfigurationEndpoint {


    private final DiscordClubConfigurationService clubConfigurationService;
    private final DiscordClubConfigurationFactory discordClubConfigurationFactory;
    private final DiscordGateway discordGateway;
    private final ApplicationEventPublisher eventPublisher;

    @GetMapping("/api_v2/configuration/channels")
    public List<DiscordClubConfigurationTo> getConfigurations() {
        return clubConfigurationService.getConfigurations().stream().map(discordClubConfigurationFactory::create).toList();
    }


    @PostMapping("/api_v2/configuration/channels")
    public void requestConfiguration(@RequestBody DiscordClubCreateConfigurationTo createConfiguration) {
        this.clubConfigurationService.createConfiguration(createConfiguration.guildId(), createConfiguration.channelId(), createConfiguration.clubId(), createConfiguration.autoPosting());
        discordGateway.createMessage(1173372471207018576L, new SimpleDiscordMessageTo("Configuration created for channelId (%s), club (%s) , autoposting: (%s)".formatted(createConfiguration.channelId(), createConfiguration.clubId(), createConfiguration.autoPosting()), List.of()));

    }

    @DeleteMapping("/api_v2/configuration/channels")
    public void removeChannelConfiguration(@RequestBody DiscordClubRemoveConfigurationTo request) {
        this.clubConfigurationService.removeConfiguration(request.channelId(), request.clubId());
        discordGateway.createMessage(1173372471207018576L, new SimpleDiscordMessageTo("Configuration removed for channelId (%s), club (%s).".formatted(request.channelId(), request.clubId()), List.of()));
        eventPublisher.publishEvent(new ConfigurationUpdatedEvent());
    }
}
