package io.busata.fourleft.endpoints.frontend.discord_integration.feign;

import io.busata.fourleft.endpoints.frontend.discord_integration.DiscordGuildTo;
import io.busata.fourleft.endpoints.frontend.discord_integration.GuildMemberTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface DiscordApi {

    @GetMapping("/users/@me/guilds")
    List<DiscordGuildTo> getGuilds();

    @GetMapping("/guilds/{guildId}/members/{userId}")
    GuildMemberTo getGuildMember(@PathVariable(name="guildId") String guildId, @PathVariable(name = "userId") String userId);
}
