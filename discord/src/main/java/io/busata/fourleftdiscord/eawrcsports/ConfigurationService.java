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

    // Refreshed lazily, but primarily invalidated via invalidate() on the EA_SPORTS_WRC_READY event
    // (published by the backend whenever a channel config is created/updated/removed). The hourly check
    // is only a fallback in case that event is ever missed.
    public List<DiscordClubConfigurationTo> getConfigurations() {

        if (configurations.isEmpty() || lastUpdated == null || Duration.between(lastUpdated, LocalDateTime.now()).toHours() > 1) {
            this.configurations = api.getConfigurations();
            this.lastUpdated = LocalDateTime.now();
        }
        return this.configurations;
    }

    // Drop the cached channel->club mapping so the next getConfigurations() reloads it from the backend.
    public void invalidate() {
        this.configurations = new ArrayList<>();
        this.lastUpdated = null;
    }

    public Optional<String> getClubId(Long channelId) {
        return getConfigurations().stream().filter(configurations -> Objects.equals(configurations.channelId(), channelId)).map(DiscordClubConfigurationTo::clubId).findFirst();
    }
}
