package io.busata.fourleft.domain.discord.integration.models;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserDiscordGuildAccessRepository extends JpaRepository<UserDiscordGuildAccess, UUID> {


    List<UUID> findUserIdByGuildIdsContaining(String guildId);
}
