package io.busata.fourleft.endpoints.frontend.discord_integration.feign.bot;


import io.busata.fourleft.endpoints.frontend.discord_integration.feign.DiscordApi;
import io.busata.fourleft.endpoints.frontend.discord_integration.feign.user.DiscordUserFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name="discordbotclient", url="https://discord.com/api/", configuration = DiscordBotFeignConfig.class)
public interface DiscordBotClient extends DiscordApi {

}
