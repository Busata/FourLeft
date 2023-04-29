package io.busata.fourleft.infrastructure.clients.discord.bot;


import io.busata.fourleft.infrastructure.clients.discord.DiscordApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name="discordbotclient", url="https://discord.com/api/", configuration = DiscordBotFeignConfig.class)
public interface DiscordBotClient extends DiscordApi {

}
