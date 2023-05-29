package io.busata.fourleft.infrastructure.clients.discord;

import io.busata.fourleft.api.models.discord.DiscordChannelTo;
import io.busata.fourleft.api.models.discord.DiscordGuildTo;
import io.busata.fourleft.api.models.discord.DiscordMemberTo;
import io.busata.fourleft.api.models.discord.DiscordUserTo;
import io.busata.fourleft.api.models.discord.GuildMemberTo;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface DiscordApi {


    @GetMapping("/users/@me")
    DiscordUserTo getMe();

    @GetMapping("/users/@me/guilds")
    List<DiscordGuildTo> getGuilds();

    @GetMapping("/guilds/{guildId}/members/{userId}")
    GuildMemberTo getGuildMember(@PathVariable(name="guildId") String guildId, @PathVariable(name = "userId") String userId);

    @GetMapping("/guilds/{guildId}")
    DiscordGuildTo getGuild(@PathVariable(name="guildId") String guildId);

    @GetMapping("/guilds/{guildId}/channels")
    List<DiscordChannelTo> getChannels(@PathVariable(name="guildId") String guildId);

    @GetMapping("/guilds/{guildId}/members")
    List<DiscordMemberTo> getMembers(@PathVariable(name="guildId") String guildId, @RequestParam(required = false, defaultValue = "10") int limit, @RequestParam(required = false, defaultValue = "") String after);
}
