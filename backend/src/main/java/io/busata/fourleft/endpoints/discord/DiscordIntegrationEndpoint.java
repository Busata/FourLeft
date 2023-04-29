package io.busata.fourleft.endpoints.discord;

import io.busata.fourleft.api.Routes;
import io.busata.fourleft.api.models.discord.DiscordAuthenticationStatusTo;
import io.busata.fourleft.api.models.discord.DiscordChannelSummaryTo;
import io.busata.fourleft.api.models.discord.DiscordGuildPermissionTo;
import io.busata.fourleft.api.models.discord.DiscordGuildSummaryTo;
import io.busata.fourleft.api.models.discord.DiscordGuildTo;
import io.busata.fourleft.application.discord.DiscordIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class DiscordIntegrationEndpoint {
    private final DiscordIntegrationService discordIntegrationService;



    @GetMapping(Routes.DISCORD_MANAGE_SERVER)
    public DiscordGuildPermissionTo canManage(@PathVariable(name="guildId") String guildId) {
        return new DiscordGuildPermissionTo(discordIntegrationService.canManage(guildId));
    }

    @GetMapping(Routes.DISCORD_INVITE_BOT)
    @ResponseStatus(HttpStatus.FOUND)
    public void inviteBot(@RequestParam(name="guild_id") String guildId, HttpServletResponse response) {
        response.setHeader(
                "Location",
                this.discordIntegrationService.enrichInviteUrl(guildId)
        );
    }

    @GetMapping(Routes.DISCORD_REDIRECT)
    @ResponseStatus(HttpStatus.FOUND)
    public void startAuthentication(HttpServletResponse response) {
        response.setHeader("Location", this.discordIntegrationService.getAuthenticationUrl());
    }

    @GetMapping(Routes.DISCORD_AUTHENTICATION_STATUS)
    public DiscordAuthenticationStatusTo getAuthenticationStatus() {
        return new DiscordAuthenticationStatusTo(
                this.discordIntegrationService.isAuthenticated()
        );
    }

    @PostMapping(Routes.DISCORD_INTEGRATION_AUTH)
    public void authenticate(@RequestBody String code) {
        this.discordIntegrationService.fetchAndStoreAccessToken(code);
    }

    @GetMapping(Routes.DISCORD_GUILDS)
    public List<DiscordGuildSummaryTo> getGuildSummary() {
        return this.discordIntegrationService.getGuildSummaries();
    }

    @GetMapping(Routes.DISCORD_GUILD)
    public DiscordGuildTo getGuild(@PathVariable(name="guildId") String guildId) {
        return this.discordIntegrationService.getGuild(guildId);
    }
    @GetMapping(Routes.DISCORD_GUILD_CHANNELS)
    public List<DiscordChannelSummaryTo> getChannels(@PathVariable(name="guildId") String guildId) {
        return this.discordIntegrationService.getGuildChannels(guildId);
    }

}
