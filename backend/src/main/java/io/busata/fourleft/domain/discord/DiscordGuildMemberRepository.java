package io.busata.fourleft.domain.discord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DiscordGuildMemberRepository extends JpaRepository<DiscordGuildMember, UUID> {

    void deleteByGuildId(String id);

    List<DiscordGuildMember> findByGuildId(String guildId);

    @Query(value = "select distinct * from discord_guild_member where discord_id in (select user_discord_guild_access_discord_id as \"id\" from user_discord_guild_access_guild_ids where guild_ids = :guildId)", nativeQuery = true)
    List<DiscordGuildMember> findGuildAdministrators(@Param("guildId") String guildId);
}
