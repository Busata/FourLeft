package io.busata.fourleft.domain.discord.integration.models;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DiscordIntegrationAccessTokensRepository extends JpaRepository<DiscordIntegrationAccessToken, UUID> {

    Optional<DiscordIntegrationAccessToken> findByUserName(String username);
}