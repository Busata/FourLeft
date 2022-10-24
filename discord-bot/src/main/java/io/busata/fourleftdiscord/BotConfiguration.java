package io.busata.fourleftdiscord;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class BotConfiguration {
    @Value("${discord.token}")
    private String discordToken;

    @Bean
    public GatewayDiscordClient gatewayDiscordClient() {
        GatewayDiscordClient client =  DiscordClientBuilder.create(discordToken)
                .build()
                .gateway().setInitialPresence(shard -> ClientPresence.online(ClientActivity.watching("the leaderboards")))
                .login().block();

        return client;
    }
}
