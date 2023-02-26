package io.busata.fourleft.endpoints.frontend.discord_integration.feign.user;

import feign.RequestInterceptor;
import io.busata.fourleft.domain.discord.integration.models.DiscordIntegrationAccessToken;
import io.busata.fourleft.domain.discord.integration.models.DiscordIntegrationAccessTokensRepository;
import io.busata.fourleft.endpoints.frontend.discord_integration.DiscordIntegrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
    import org.springframework.security.core.context.SecurityContextHolder;

@RequiredArgsConstructor
public class DiscordUserFeignConfig {
    private final DiscordIntegrationAccessTokensRepository repository;

    @Bean
    public RequestInterceptor discordRequestInterceptor() {
        return requestTemplate -> {
            final var authentication = SecurityContextHolder.getContext().getAuthentication();

            final var token = this.repository.findByUserName(authentication.getName()).orElseThrow();

            requestTemplate.header("Authorization", "Bearer " + token.getAccessToken());
        };
    }
}
