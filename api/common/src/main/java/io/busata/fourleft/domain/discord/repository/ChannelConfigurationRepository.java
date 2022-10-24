package io.busata.fourleft.domain.discord.repository;

import io.busata.fourleft.domain.discord.models.ChannelConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChannelConfigurationRepository extends JpaRepository<ChannelConfiguration, UUID> {
}
