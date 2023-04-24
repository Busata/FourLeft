package io.busata.fourleft.domain.discord.integration.models;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserDiscordGuildAccessRepository extends JpaRepository<UserDiscordGuildAccess, UUID> {
}