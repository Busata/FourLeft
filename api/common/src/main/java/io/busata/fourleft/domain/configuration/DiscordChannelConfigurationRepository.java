package io.busata.fourleft.domain.configuration;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DiscordChannelConfigurationRepository extends JpaRepository<DiscordChannelConfiguration, UUID> {

    Optional<DiscordChannelConfiguration> findByChannelId(Long channelId);
}