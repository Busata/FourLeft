package io.busata.fourleft.endpoints.discord.integration.feign.bot;

import feign.RequestInterceptor;
import io.busata.fourleft.endpoints.discord.integration.DiscordIntegrationConfigurationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;

@RequiredArgsConstructor
public class DiscordBotFeignConfig {
    private final DiscordIntegrationConfigurationProperties properties;

    @Bean
    public RequestInterceptor discordRequestInterceptor() {
        return requestTemplate -> {

            requestTemplate.header("Authorization", "Bot " + properties.getBotToken());
        };
    }
}