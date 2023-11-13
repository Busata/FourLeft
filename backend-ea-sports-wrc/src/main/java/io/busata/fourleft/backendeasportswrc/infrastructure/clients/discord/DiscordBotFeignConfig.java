package io.busata.fourleft.backendeasportswrc.infrastructure.clients.discord;

import feign.RequestInterceptor;
import feign.okhttp.OkHttpClient;
import io.busata.fourleft.backendeasportswrc.infrastructure.properties.DiscordIntegrationConfigurationProperties;
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

    @Bean
    public OkHttpClient client() {
        return new OkHttpClient();
    }
}
