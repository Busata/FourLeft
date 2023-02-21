package io.busata.fourleft.endpoints.frontend.discord_integration;

import io.busata.fourleft.domain.discord.integration.models.DiscordIntegrationAccessToken;
import io.busata.fourleft.domain.discord.integration.models.DiscordIntegrationAccessTokensRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DiscordIntegrationService {

    private final DiscordIntegrationAccessTokensRepository discordIntegrationAccessTokensRepository;

    public void setToken(String username, DiscordTokenTo token) {
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

    public Optional<DiscordIntegrationAccessToken> getToken(String username) {
        return this.discordIntegrationAccessTokensRepository.findByUserName(username);
    }
}
