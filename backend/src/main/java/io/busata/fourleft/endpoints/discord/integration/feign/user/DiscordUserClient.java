package io.busata.fourleft.endpoints.discord.integration.feign.user;


import io.busata.fourleft.endpoints.discord.integration.feign.DiscordApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name="discorduserclient", url="https://discord.com/api/", configuration = DiscordUserFeignConfig.class)
public interface DiscordUserClient extends DiscordApi {

}
