package io.busata.fourleft.endpoints.discord.integration.feign.bot;


import io.busata.fourleft.endpoints.discord.integration.feign.DiscordApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name="discordbotclient", url="https://discord.com/api/", configuration = DiscordBotFeignConfig.class)
public interface DiscordBotClient extends DiscordApi {

}
