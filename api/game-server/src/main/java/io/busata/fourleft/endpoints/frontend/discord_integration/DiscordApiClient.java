package io.busata.fourleft.endpoints.frontend.discord_integration;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name="discordapi", url="https://discord.com/api/", configuration = DiscordClientConfig.class)
public interface DiscordApiClient {

    @GetMapping("/users/@me/guilds")
    List<DiscordGuildTo> getGuilds();
}
