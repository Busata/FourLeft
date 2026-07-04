package io.busata.fourleft.backendeasportswrc.application.discord.configuration;

import io.busata.fourleft.backendeasportswrc.domain.models.configuration.ChannelConfigurationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface ChannelConfigurationRequestRepository extends JpaRepository<ChannelConfigurationRequest, UUID> {
}
