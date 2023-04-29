package io.busata.fourleft.infrastructure.clients.discord.user;


import io.busata.fourleft.infrastructure.clients.discord.DiscordApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name="discorduserclient", url="https://discord.com/api/", configuration = DiscordUserFeignConfig.class)
public interface DiscordUserClient extends DiscordApi {

}
