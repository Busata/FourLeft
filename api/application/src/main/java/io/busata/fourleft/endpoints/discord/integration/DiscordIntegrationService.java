package io.busata.fourleft.endpoints.discord.integration;

import io.busata.fourleft.api.models.discord.DiscordChannelSummaryTo;
import io.busata.fourleft.api.models.discord.DiscordChannelTo;
import io.busata.fourleft.api.models.discord.DiscordGuildSummaryTo;
import io.busata.fourleft.api.models.discord.DiscordGuildTo;
import io.busata.fourleft.api.models.discord.DiscordTokenTo;
import io.busata.fourleft.domain.configuration.DiscordChannelConfigurationRepository;
import io.busata.fourleft.domain.discord.integration.models.DiscordIntegrationAccessToken;
import io.busata.fourleft.domain.discord.integration.models.DiscordIntegrationAccessTokensRepository;
import io.busata.fourleft.domain.discord.integration.models.UserDiscordGuildAccessRepository;
import io.busata.fourleft.endpoints.discord.integration.feign.auth.DiscordOauth2Client;
import io.busata.fourleft.endpoints.discord.integration.feign.user.DiscordUserClient;
import io.busata.fourleft.endpoints.discord.integration.feign.bot.DiscordBotClient;
import io.busata.fourleft.endpoints.security.FourLeftRole;
import io.busata.fourleft.endpoints.security.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscordIntegrationService {

    private final DiscordChannelConfigurationRepository discordChannelConfigurationRepository;
    private final DiscordIntegrationAccessTokensRepository discordIntegrationAccessTokensRepository;
    private final DiscordOauth2Client discordOauth2Client;
    private final DiscordIntegrationConfigurationProperties discordIntegrationConfigurationProperties;

    private final DiscordBotClient discordBotClient;
    private final DiscordUserClient discordUserClient;

    private final SecurityService securityService;

    private final UserDiscordGuildAccessRepository userDiscordGuildAccessRepository;

    private final DiscordGuildSummaryToFactory discordGuildSummaryToFactory;

    public List<DiscordGuildSummaryTo> getGuildSummaries() {
        List<DiscordGuildTo> botGuilds = this.discordBotClient.getGuilds();

        List<DiscordGuildSummaryTo> discordGuildSummaryTos = Stream.of(isAuthenticated())
                .filter(x -> x)
                .flatMap(authenticated -> {
                    List<DiscordGuildTo> userGuilds = this.discordUserClient.getGuilds();

                    return userGuilds.stream()
                            .filter(DiscordGuildTo::canManageServer)
                            .map(guild -> {
                                boolean botJoined = botGuilds.contains(guild);
                                return discordGuildSummaryToFactory.create(guild, botJoined);
                            });
                }).toList();

        return Stream.concat(
                botGuilds.stream()
                        .filter(guild -> canManage(guild.id()))
                        .map(guild -> discordGuildSummaryToFactory.create(guild, true))
                ,discordGuildSummaryTos.stream()
        ).distinct().toList();

    }

    public boolean isAuthenticated() {
       return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .map(Principal::getName)
                .flatMap(this.discordIntegrationAccessTokensRepository::findByUserName)
                .map(this::verifyToken)
                .orElse(false);
    }

    public boolean verifyToken(DiscordIntegrationAccessToken token) {
        if (LocalDateTime.now().plusMinutes(5).isBefore(token.getExpireDate())) {
            return true;
        }

        return refreshAndCheck(token);
    }

    private boolean refreshAndCheck(DiscordIntegrationAccessToken token) {
        try {
            storeToken(refreshToken(token.getRefreshToken()));
            return true;
        } catch (Exception e) {
            log.error("Error refreshing token", e);
            return false;
        }
    }

    public void fetchAndStoreAccessToken(String code) {
        storeToken(getAccessToken(code));
    }

    public void storeToken(DiscordTokenTo token) {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        discordIntegrationAccessTokensRepository.findByUserName(username).ifPresentOrElse(
                discordIntegrationAccessToken -> {
                    discordIntegrationAccessToken.setAccessToken(token.access_token());
                    discordIntegrationAccessToken.setRefreshToken(token.refresh_token());
                    discordIntegrationAccessToken.setScope(token.scope());
                    discordIntegrationAccessToken.setExpireDate(LocalDateTime.now().plusSeconds(token.expires_in()));

                    discordIntegrationAccessTokensRepository.save(discordIntegrationAccessToken);
                },
                () -> {
                    var discordIntegrationAccessToken = new DiscordIntegrationAccessToken(username, token.access_token(), token.refresh_token(), LocalDateTime.now().plusSeconds(token.expires_in()), token.scope());
                    discordIntegrationAccessTokensRepository.save(discordIntegrationAccessToken);
                });
    }

    private DiscordTokenTo getAccessToken(String code) {
        String body = Map.of(
                            "client_id", discordIntegrationConfigurationProperties.getClientId(),
                        "client_secret", discordIntegrationConfigurationProperties.getClientSecret(),
                        "code", code,
                        "grant_type", "authorization_code",
                        "redirect_uri", discordIntegrationConfigurationProperties.getRedirectUri(),
                        "scope", "identify guilds"
                ).entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .reduce((s, s2) -> s + "&" + s2).orElseThrow();

        return this.discordOauth2Client.requestToken(body);
    }

    public DiscordTokenTo getBotToken() {
        String body = Map.of(
                            "client_id", discordIntegrationConfigurationProperties.getClientId(),
                        "client_secret", discordIntegrationConfigurationProperties.getClientSecret(),
                        "grant_type", "client_credentials",
                        "scope", "identify guilds"
                ).entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .reduce((s, s2) -> s + "&" + s2).orElseThrow();

        return this.discordOauth2Client.requestToken(body);
    }
    private DiscordTokenTo refreshToken(String refreshToken) {
        String body = Map.of(
                            "client_id", discordIntegrationConfigurationProperties.getClientId(),
                        "client_secret", discordIntegrationConfigurationProperties.getClientSecret(),
                        "refresh_token", refreshToken,
                        "grant_type", "refresh_token"
                ).entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .reduce((s, s2) -> s + "&" + s2).orElseThrow();

        return this.discordOauth2Client.requestToken(body);
    }


    public DiscordGuildTo getGuild(String guildId) {
        return this.discordBotClient.getGuild(guildId);
    }


    public List<DiscordChannelSummaryTo> getGuildChannels(String guildId) {
        return this.discordBotClient.getChannels(guildId).stream()
                .filter(channel -> channel.type() == DisordChannelType.TEXT.getType())
                .map(channel -> {
                    boolean hasConfiguration = discordChannelConfigurationRepository.findByChannelId(Long.parseLong(channel.id())).isPresent();
                    return new DiscordChannelSummaryTo(channel.id(), channel.name(), hasConfiguration);
                })
                .toList();
    }

    public boolean canManage(String guildId) {
        final var isAdmin = securityService.userHasRole(FourLeftRole.ADMIN);

        final var userAccess = userDiscordGuildAccessRepository.findById(securityService.getUserId());

        return userAccess.map(user -> user.getGuildIds().contains(guildId)).orElse(false) || isAdmin;

    }
}
