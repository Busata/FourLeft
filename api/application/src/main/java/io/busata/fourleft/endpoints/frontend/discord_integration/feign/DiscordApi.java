package io.busata.fourleft.endpoints.frontend.discord_integration.feign;

import io.busata.fourleft.endpoints.frontend.discord_integration.DiscordChannelTo;
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

    @GetMapping("/guilds/{guildId}")
    DiscordGuildTo getGuild(@PathVariable(name="guildId") String guildId);

    @GetMapping("/guilds/{guildId}/channels")
    List<DiscordChannelTo> getChannels(@PathVariable(name="guildId") String guildId);

    @GetMapping("/guilds/{guildId}/members")
    List<DiscordMemberTo> getMembers(@PathVariable(name="guildId") String guildId);
}
