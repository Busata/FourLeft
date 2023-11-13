package io.busata.fourleft.backendeasportswrc.application.discord.configuration;

import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscordClubConfigurationService {

    private final DiscordClubConfigurationRepository repository;


    @Transactional(readOnly = true)
    public List<DiscordClubConfiguration> findByClubId(String clubId) {
        return this.repository.findByClubId(clubId);
    }

    @Transactional(readOnly = true)
    public List<DiscordClubConfiguration> getConfigurations() {
        return this.repository.findAll();
    }

}
