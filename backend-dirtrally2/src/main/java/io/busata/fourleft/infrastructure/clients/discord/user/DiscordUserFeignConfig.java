package io.busata.fourleft.infrastructure.clients.discord.user;

import feign.RequestInterceptor;
import io.busata.fourleft.domain.discord.integration.models.DiscordIntegrationAccessTokensRepository;
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
