package io.busata.fourleft.backendeasportswrc.endpoints;

import io.busata.fourleft.api.easportswrc.events.ConfigurationUpdatedEvent;
import io.busata.fourleft.api.easportswrc.models.ChannelConfigurationRequestResultTo;
import io.busata.fourleft.api.easportswrc.models.ChannelConfigurationRequestTo;
import io.busata.fourleft.api.easportswrc.models.ChannelConfigurationTo;
import io.busata.fourleft.api.easportswrc.models.DiscordClubConfigurationTo;
import io.busata.fourleft.api.easportswrc.models.DiscordClubCreateConfigurationTo;
import io.busata.fourleft.api.easportswrc.models.DiscordClubRemoveConfigurationTo;
import io.busata.fourleft.backendeasportswrc.application.discord.configuration.ChannelConfigurationRequestService;
import io.busata.fourleft.backendeasportswrc.application.discord.configuration.DiscordClubConfigurationService;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.DiscordGateway;
import io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord.models.SimpleDiscordMessageTo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ConfigurationEndpoint {


    private final DiscordClubConfigurationService clubConfigurationService;
    private final ChannelConfigurationRequestService channelConfigurationRequestService;
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

    @PostMapping("/api_v2/configuration/channel/request")
    public ChannelConfigurationRequestResultTo requestChannelConfiguration(@RequestBody ChannelConfigurationRequestTo request) {
        UUID requestId = this.channelConfigurationRequestService.requestConfiguration(request.guildId(), request.channelId(), request.discordId());
        return new ChannelConfigurationRequestResultTo(requestId);
    }

    @GetMapping("/api_v2/configuration/channel/{requestId}")
    public Optional<ChannelConfigurationTo> getChannelConfiguration(@PathVariable UUID requestId) {
        return this.channelConfigurationRequestService.getConfiguration(requestId);
    }
}
