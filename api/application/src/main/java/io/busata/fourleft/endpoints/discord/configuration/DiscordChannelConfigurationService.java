package io.busata.fourleft.endpoints.discord.configuration;


import io.busata.fourleft.domain.configuration.DiscordChannelConfiguration;
import io.busata.fourleft.domain.configuration.DiscordChannelConfigurationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DiscordChannelConfigurationService {
    private final DiscordChannelConfigurationRepository discordChannelConfigurationRepository;


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
}
