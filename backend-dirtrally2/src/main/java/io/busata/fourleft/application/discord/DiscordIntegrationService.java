package io.busata.fourleft.application.discord;

import io.busata.fourleft.api.models.discord.DiscordChannelSummaryTo;
import io.busata.fourleft.api.models.discord.DiscordGuildSummaryTo;
import io.busata.fourleft.api.models.discord.DiscordGuildTo;
import io.busata.fourleft.api.models.discord.DiscordTokenTo;
import io.busata.fourleft.api.models.discord.DiscordUserTo;
import io.busata.fourleft.common.MemberEvent;
import io.busata.fourleft.domain.discord.DiscordChannelConfigurationRepository;
import io.busata.fourleft.domain.discord.DiscordGuildMember;
import io.busata.fourleft.domain.discord.DiscordGuildMemberRepository;
import io.busata.fourleft.domain.discord.integration.models.DiscordIntegrationAccessToken;
import io.busata.fourleft.domain.discord.integration.models.DiscordIntegrationAccessTokensRepository;
import io.busata.fourleft.domain.discord.integration.models.UserDiscordGuildAccess;
import io.busata.fourleft.domain.discord.integration.models.UserDiscordGuildAccessRepository;
import io.busata.fourleft.infrastructure.configuration.properties.DiscordIntegrationConfigurationProperties;
import io.busata.fourleft.domain.discord.DisordChannelType;
import io.busata.fourleft.infrastructure.clients.discord.auth.DiscordOauth2Client;
import io.busata.fourleft.infrastructure.clients.discord.user.DiscordUserClient;
import io.busata.fourleft.infrastructure.clients.discord.bot.DiscordBotClient;
import io.busata.fourleft.domain.infrastructure.FourLeftRole;
import io.busata.fourleft.application.security.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
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
    private final DiscordGuildMemberRepository discordGuildMemberRepository;

    public String enrichInviteUrl(String guildId) {
        return discordIntegrationConfigurationProperties.getBotInviteUrl()
                .replace("${guildId}", guildId)
                .replace("${clientId}", discordIntegrationConfigurationProperties.getClientId());
    }

    public String getAuthenticationUrl() {
        return discordIntegrationConfigurationProperties.getAuthUrl();
    }

    public List<DiscordGuildSummaryTo> getGuildSummaries() {
        List<DiscordGuildTo> botGuilds = this.discordBotClient.getGuilds();

        boolean isUserAuthenticated = isAuthenticated();

        List<DiscordGuildSummaryTo> discordGuildSummaryTos = Stream.of(isUserAuthenticated)
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

        Optional<UserDiscordGuildAccess> userAccess = Optional.of(isUserAuthenticated).filter(x -> x).flatMap(x -> {
            DiscordUserTo me = discordUserClient.getMe();
            return userDiscordGuildAccessRepository.findById(me.id());
        });

        return Stream.concat(
                botGuilds.stream()
                        .filter(guild -> {
                            if (!isUserAuthenticated) {
                                return false;
                            }

                            if (isAdmin()) {
                                return true;
                            }

                            return userAccess.map(user -> user.getGuildIds().contains(guild.id())).orElse(false);

                        })
                        .map(guild -> discordGuildSummaryToFactory.create(guild, true))
                , discordGuildSummaryTos.stream()
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

    public List<DiscordGuildMember> getMembers(String guildId) {
        return discordGuildMemberRepository.findByGuildId(guildId);
    }

    public boolean canManage(String guildId) {
        if (!isAuthenticated()) {
            return false;
        }

        if (isAdmin()) {
            return true;
        }

        if (canManageServer(guildId)) {
            return true;
        }

        return hasAccessRights(guildId);

    }

    private Boolean hasAccessRights(String guildId) {
        DiscordUserTo me = discordUserClient.getMe();

        final var userAccess = userDiscordGuildAccessRepository.findById(me.id());

        return userAccess.map(user -> user.getGuildIds().contains(guildId)).orElse(false);
    }

    private boolean isAdmin() {
        return securityService.userHasRole(FourLeftRole.ADMIN);
    }

    private boolean canManageServer(String guildId) {
        List<DiscordGuildTo> userGuilds = this.discordUserClient.getGuilds();

        return userGuilds.stream()
                .filter(DiscordGuildTo::canManageServer)
                .anyMatch(guild -> guild.id().equals(guildId));
    }

    public List<DiscordGuildMember> getAdministrators(String guildId) {
        return discordGuildMemberRepository.findGuildAdministrators(guildId).stream().distinct().toList();
    }

    public DiscordGuildMember addAccess(String guildId, UUID userId) {
        final var member = this.discordGuildMemberRepository.findById(userId).orElseThrow();

        UserDiscordGuildAccess userDiscordGuildAccess = this.userDiscordGuildAccessRepository.findById(member.getDiscordId()).map(existingAccess -> {
            existingAccess.getGuildIds().add(guildId);
            return existingAccess;
        }).orElseGet(() -> {
            return new UserDiscordGuildAccess(member.getDiscordId(), List.of(guildId));
        });

        this.userDiscordGuildAccessRepository.save(userDiscordGuildAccess);

        return this.discordGuildMemberRepository.findById(userId).orElseThrow();
    }

    public DiscordGuildMember removeAccess(String guildId, UUID userId) {
        final var member = this.discordGuildMemberRepository.findById(userId).orElseThrow();

        UserDiscordGuildAccess userDiscordGuildAccess = this.userDiscordGuildAccessRepository.findById(member.getDiscordId()).map(existingAccess -> {
            existingAccess.getGuildIds().remove(guildId);
            return existingAccess;
        }).orElseThrow();


        this.userDiscordGuildAccessRepository.save(userDiscordGuildAccess);

        return this.discordGuildMemberRepository.findById(userId).orElseThrow();
    }

    @Transactional
    public void handleMemberEvent(String guildId, String discordId, String username, MemberEvent memberEvent) {
        log.info("Member event: {} joined {} (id: {}", username, guildId, discordId);
        switch (memberEvent) {
            case JOINED -> {
                this.discordGuildMemberRepository.save(new DiscordGuildMember(discordId, username, guildId));
            }
            case LEFT -> {
                this.discordGuildMemberRepository.deleteByGuildIdAndDiscordId(guildId, discordId);
            }
        }
    }
}
