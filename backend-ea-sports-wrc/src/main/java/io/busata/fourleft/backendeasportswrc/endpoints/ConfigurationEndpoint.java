package io.busata.fourleft.backendeasportswrc.endpoints;

import io.busata.fourleft.api.easportswrc.models.DiscordClubConfigurationTo;
import io.busata.fourleft.backendeasportswrc.application.discord.configuration.DiscordClubConfigurationService;
import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ConfigurationEndpoint {

    private final DiscordClubConfigurationService clubConfigurationService;
    private final DiscordClubConfigurationFactory discordClubConfigurationFactory;

    @GetMapping("/api/configuration/channels")
    public List<DiscordClubConfigurationTo> getConfigurations() {
        return clubConfigurationService.getConfigurations().stream().map(discordClubConfigurationFactory::create).toList();

    }
}
