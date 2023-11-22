package io.busata.fourleft.backendeasportswrc.application.discord.configuration;

import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DiscordClubConfigurationService {

    private final DiscordClubConfigurationRepository repository;


    @Transactional(readOnly = true)
    public List<DiscordClubConfiguration> findByClubId(String clubId) {
        return this.repository.findByClubId(clubId);
    }

    @Transactional(readOnly = true)
    public Optional<DiscordClubConfiguration> findByChannelId(Long channelId) {
        return this.repository.findByChannelId(channelId);
    }

    @Transactional(readOnly = true)
    public List<DiscordClubConfiguration> getConfigurations() {
        return this.repository.findAll();
    }

    @Transactional
    public void createConfiguration(Long guildId, Long channelID, String clubId, boolean autoPostingEnabled) {
        this.repository.save(new DiscordClubConfiguration(
                guildId,
                channelID,
                clubId,
                autoPostingEnabled
        ));
    }
}
