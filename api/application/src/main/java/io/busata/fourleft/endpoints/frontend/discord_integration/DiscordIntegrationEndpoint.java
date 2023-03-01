package io.busata.fourleft.endpoints.frontend.discord_integration;

import io.busata.fourleft.api.Routes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DiscordIntegrationEndpoint {
    private final DiscordIntegrationService discordIntegrationService;

    private final DiscordIntegrationConfigurationProperties discordProperties;


    @GetMapping(Routes.DISCORD_MANAGE_SERVER)
    @DiscordAuthenticated
    public DiscordGuildPermissionTo canManage(@PathVariable(name="guildId") String guildId) {
        return new DiscordGuildPermissionTo(true);
    }

    @GetMapping(Routes.DISCORD_INVITE_BOT)
    @ResponseStatus(HttpStatus.FOUND)
    public void inviteBot(@RequestParam(name="guild_id") String guildId, HttpServletResponse response) {
        response.setHeader(
                "Location",
                enrichInviteUrl(discordProperties.getBotInviteUrl(), guildId, discordProperties.getClientId())
        );
    }

    private String enrichInviteUrl(String baseBotInviteUrl, String guildId, String clientId) {
        return baseBotInviteUrl
                .replace("${guildId}", guildId)
                .replace("${clientId}", clientId);
    }

    @GetMapping(Routes.DISCORD_REDIRECT)
    @ResponseStatus(HttpStatus.FOUND)
    public void startAuthentication(HttpServletResponse response) {
        response.setHeader("Location", discordProperties.getAuthUrl());
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
    @DiscordAuthenticated
    public List<DiscordGuildSummaryTo> getGuildSummary() {
        return this.discordIntegrationService.getGuildSummaries();
    }

    @GetMapping(Routes.DISCORD_GUILD)
    @DiscordAuthenticated
    public DiscordGuildTo getGuild(@PathVariable(name="guildId") String guildId) {
        return this.discordIntegrationService.getGuild(guildId);
    }
    @GetMapping(Routes.DISCORD_GUILD_CHANNELS)
    @DiscordAuthenticated
    public List<DiscordChannelTo> getChannels(@PathVariable(name="guildId") String guildId) {
        return this.discordIntegrationService.getGuildChannels(guildId);
    }

}
