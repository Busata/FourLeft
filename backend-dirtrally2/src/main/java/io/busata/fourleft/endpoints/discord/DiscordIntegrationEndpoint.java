package io.busata.fourleft.endpoints.discord;

import io.busata.fourleft.api.RoutesTo;
import io.busata.fourleft.api.models.discord.DiscordAuthenticationStatusTo;
import io.busata.fourleft.api.models.discord.DiscordChannelSummaryTo;
import io.busata.fourleft.api.models.discord.DiscordGuildMemberTo;
import io.busata.fourleft.api.models.discord.DiscordGuildPermissionTo;
import io.busata.fourleft.api.models.discord.DiscordGuildSummaryTo;
import io.busata.fourleft.api.models.discord.DiscordGuildTo;
import io.busata.fourleft.application.discord.DiscordGuildMemberFactory;
import io.busata.fourleft.application.discord.DiscordIntegrationService;
import io.busata.fourleft.application.discord.DiscordMemberUpdater;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DiscordIntegrationEndpoint {
    private final DiscordIntegrationService discordIntegrationService;
    private final DiscordGuildMemberFactory discordGuildMemberFactory;
    private final DiscordMemberUpdater discordMemberUpdater;


    @GetMapping(RoutesTo.DISCORD_MANAGE_SERVER)
    public DiscordGuildPermissionTo canManage(@PathVariable(name="guildId") String guildId) {
        return new DiscordGuildPermissionTo(discordIntegrationService.canManage(guildId));
    }

    @GetMapping(RoutesTo.DISCORD_INVITE_BOT)
    @ResponseStatus(HttpStatus.FOUND)
    public void inviteBot(@RequestParam(name="guild_id") String guildId, HttpServletResponse response) {
        response.setHeader(
                "Location",
                this.discordIntegrationService.enrichInviteUrl(guildId)
        );
    }

    @GetMapping(RoutesTo.DISCORD_REDIRECT)
    @ResponseStatus(HttpStatus.FOUND)
    public void startAuthentication(HttpServletResponse response) {
        response.setHeader("Location", this.discordIntegrationService.getAuthenticationUrl());
    }

    @GetMapping(RoutesTo.DISCORD_AUTHENTICATION_STATUS)
    public DiscordAuthenticationStatusTo getAuthenticationStatus() {
        return new DiscordAuthenticationStatusTo(
                this.discordIntegrationService.isAuthenticated()
        );
    }

    @PostMapping(RoutesTo.DISCORD_INTEGRATION_AUTH)
    public void authenticate(@RequestBody String code) {
        this.discordIntegrationService.fetchAndStoreAccessToken(code);
    }

    @GetMapping(RoutesTo.DISCORD_GUILDS)
    public List<DiscordGuildSummaryTo> getGuildSummary(@RequestParam(value = "sync", required = false) Optional<String> guildId) {
        guildId.ifPresent(discordMemberUpdater::syncGuild);
        return this.discordIntegrationService.getGuildSummaries();
    }

    @GetMapping(RoutesTo.DISCORD_GUILD)
    public DiscordGuildTo getGuild(@PathVariable(name="guildId") String guildId) {
        return this.discordIntegrationService.getGuild(guildId);
    }
    @GetMapping(RoutesTo.DISCORD_GUILD_CHANNELS)
    public List<DiscordChannelSummaryTo> getChannels(@PathVariable(name="guildId") String guildId) {
        return this.discordIntegrationService.getGuildChannels(guildId);
    }
    @GetMapping(RoutesTo.DISCORD_GUILD_MEMBERS)
    public List<DiscordGuildMemberTo> getMembers(@PathVariable(name="guildId") String guildId) {
        return this.discordIntegrationService.getMembers(guildId).stream().map(discordGuildMemberFactory::create).toList();
    }
    @GetMapping(RoutesTo.DISCORD_GUILD_ADMINISTRATORS)
    public List<DiscordGuildMemberTo> getGuildAdministrators(@PathVariable(name="guildId") String guildId) {
        return this.discordIntegrationService.getAdministrators(guildId).stream().map(discordGuildMemberFactory::create).toList();
    }
    @PostMapping(RoutesTo.DISCORD_GUILD_ADMINISTRATORS_CRUD)
    public DiscordGuildMemberTo addAccess(@PathVariable(name="guildId") String guildId, @PathVariable(name="userId") UUID userId) {
        final var discordGuildMember = this.discordIntegrationService.addAccess(guildId, userId);
        return this.discordGuildMemberFactory.create(discordGuildMember);
    }

    @DeleteMapping(RoutesTo.DISCORD_GUILD_ADMINISTRATORS_CRUD)
    public DiscordGuildMemberTo removeAccess(@PathVariable(name="guildId") String guildId, @PathVariable(name="userId") UUID userId) {
        final var discordGuildMember = this.discordIntegrationService.removeAccess(guildId, userId);
        return this.discordGuildMemberFactory.create(discordGuildMember);
    }

}
