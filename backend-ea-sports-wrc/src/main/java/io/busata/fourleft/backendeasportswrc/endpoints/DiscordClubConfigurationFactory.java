package io.busata.fourleft.backendeasportswrc.endpoints;

import io.busata.fourleft.api.easportswrc.models.DiscordClubConfigurationTo;
import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import org.springframework.stereotype.Component;

@Component
public class DiscordClubConfigurationFactory {

    public DiscordClubConfigurationTo create(DiscordClubConfiguration configuration) {
        return new DiscordClubConfigurationTo(
                configuration.getChannelId(),
                configuration.getClubId()
        );
    }
}
