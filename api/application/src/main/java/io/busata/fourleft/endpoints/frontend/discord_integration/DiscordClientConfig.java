package io.busata.fourleft.endpoints.frontend.discord_integration;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
    import org.springframework.security.core.context.SecurityContextHolder;

@RequiredArgsConstructor
public class DiscordClientConfig {
    private final DiscordIntegrationService discordIntegrationService;

    @Bean
    public RequestInterceptor discordRequestInterceptor() {
        return requestTemplate -> {
            final var authentication = SecurityContextHolder.getContext().getAuthentication();

            final var token = this.discordIntegrationService.getToken(authentication.getName()).orElseThrow();

            requestTemplate.header("Authorization", "Bearer " + token.getAccessToken());
        };
    }
}
