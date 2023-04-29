package io.busata.fourleft.application.discord;


import io.busata.fourleft.domain.aggregators.ClubViewRepository;
import io.busata.fourleft.domain.aggregators.DiscordChannelConfiguration;
import io.busata.fourleft.domain.aggregators.DiscordChannelConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DiscordChannelConfigurationService {
    private final DiscordChannelConfigurationRepository discordChannelConfigurationRepository;
    private final ClubViewRepository clubViewRepository;


    public List<DiscordChannelConfiguration> findAll() {
        return discordChannelConfigurationRepository.findAll();
    }

    @Transactional
    public UUID createConfiguration(DiscordChannelConfiguration configuration)  {
        discordChannelConfigurationRepository.findByChannelId(configuration.getChannelId())
                .ifPresent(discordChannelConfigurationRepository::delete);


        final var savedEntity = discordChannelConfigurationRepository.save(configuration);

        return savedEntity.getId();
    }

    public Optional<DiscordChannelConfiguration> findConfigurationByChannelId(Long channelId) {
        return this.discordChannelConfigurationRepository.findByChannelId(channelId);
    }

    @Transactional
    public void deleteConfigurationByChannelId(UUID configurationId) {
        this.discordChannelConfigurationRepository.deleteById(configurationId);
    }
}
