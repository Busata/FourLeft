package io.busata.fourleft.endpoints.frontend.discord_integration;

import io.busata.fourleft.domain.discord.integration.models.DiscordIntegrationAccessToken;
import io.busata.fourleft.domain.discord.integration.models.DiscordIntegrationAccessTokensRepository;
import io.busata.fourleft.endpoints.frontend.discord_integration.feign.bot.DiscordBotClient;
import io.busata.fourleft.endpoints.frontend.discord_integration.feign.auth.DiscordOauth2Client;
import io.busata.fourleft.endpoints.frontend.discord_integration.feign.user.DiscordUserClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscordIntegrationService {

    private final DiscordIntegrationAccessTokensRepository discordIntegrationAccessTokensRepository;
    private final DiscordOauth2Client discordOauth2Client;
    private final DiscordIntegrationConfigurationProperties discordIntegrationConfigurationProperties;

    private final DiscordBotClient discordBotClient;
    private final DiscordUserClient discordUserClient;

    public List<DiscordGuildSummaryTo> getGuildSummaries() {
        List<String> botGuildIds = this.discordBotClient.getGuilds().stream().map(DiscordGuildTo::id).toList();

        return this.discordUserClient.getGuilds().stream()
                .filter(DiscordGuildTo::canManageServer)
                .map(guild -> {
                    boolean botJoined = botGuildIds.contains(guild.id());
                    return new DiscordGuildSummaryTo(
                            guild.id(),
                            guild.name(),
                            guild.icon(),
                            botJoined
                    );
                })
                .toList();
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


}
