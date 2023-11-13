package io.busata.fourleftdiscord.eawrcsports;

import io.busata.fourleft.api.easportswrc.models.DiscordClubConfigurationTo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConfigurationService {
    private final EAWRCBackendApi api;

    private LocalDateTime lastUpdated;
    private List<DiscordClubConfigurationTo> configurations = new ArrayList<>();

    //TODO shouldn't be cached, just updated via rabbitmq events
    public List<DiscordClubConfigurationTo> getConfigurations() {

        if (configurations.isEmpty() || lastUpdated == null || Duration.between(LocalDateTime.now(), lastUpdated).toHours() > 1) {
            this.configurations = api.getConfigurations();
            this.lastUpdated = LocalDateTime.now();
        }
        return this.configurations;
    }

    public Optional<String> getClubId(Long channelId) {
        return getConfigurations().stream().filter(configurations -> Objects.equals(configurations.channelId(), channelId)).map(DiscordClubConfigurationTo::clubId).findFirst();
    }
}
