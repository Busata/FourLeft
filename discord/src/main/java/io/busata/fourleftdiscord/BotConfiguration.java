package io.busata.fourleftdiscord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumSet;


@Configuration
public class BotConfiguration {
    @Value("${discord.token}")
    private String discordToken;

    @Bean
    public JDA jda() {
        // MESSAGE_CONTENT is privileged: it must be enabled in the Discord developer portal
        // or the gateway refuses the connection (close code 4014).
        return JDABuilder.createLight(discordToken, EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT))
                .setActivity(Activity.watching("the leaderboards"))
                .build();
    }
}
