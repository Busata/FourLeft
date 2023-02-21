package io.busata.fourleft.domain.discord.bot.repository;

import io.busata.fourleft.domain.discord.bot.models.ChannelConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChannelConfigurationRepository extends JpaRepository<ChannelConfiguration, UUID> {
}
