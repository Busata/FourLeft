package io.busata.fourleftdiscord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class BotConfiguration {
    @Value("${discord.token}")
    private String discordToken;

    @Bean
    public JDA jda() {
        return JDABuilder.createLight(discordToken)
                .setActivity(Activity.watching("the leaderboards"))
                .build();
    }
}
