package io.busata.fourleft.backendeasportswrc.application.discord.configuration;

import io.busata.fourleft.api.easportswrc.models.ChannelConfigurationTo;
import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.models.configuration.ChannelConfigurationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChannelConfigurationRequestService {

    private final ChannelConfigurationRequestRepository requestRepository;
    private final DiscordClubConfigurationRepository clubConfigurationRepository;

    @Transactional
    public UUID requestConfiguration(Long guildId, Long channelId, String discordId) {
        ChannelConfigurationRequest request = requestRepository.save(
                new ChannelConfigurationRequest(guildId, channelId, discordId));

        return request.getId();
    }

    @Transactional(readOnly = true)
    public Optional<ChannelConfigurationTo> getConfiguration(UUID requestId) {
        return requestRepository.findById(requestId).map(request -> {
            Optional<DiscordClubConfiguration> configuration = clubConfigurationRepository.findByChannelId(request.getChannelId());

            return configuration
                    .map(config -> new ChannelConfigurationTo(
                            String.valueOf(request.getGuildId()),
                            String.valueOf(request.getChannelId()),
                            true,
                            config.getClubId(),
                            config.isAutopostingEnabled(),
                            config.isRequiresTracking(),
                            config.isEnabled()
                    ))
                    .orElseGet(() -> new ChannelConfigurationTo(
                            String.valueOf(request.getGuildId()),
                            String.valueOf(request.getChannelId()),
                            false,
                            null,
                            null,
                            null,
                            null
                    ));
        });
    }
}
