package io.busata.fourleftdiscord.client;

import io.busata.fourleft.api.DiscordChannelConfigurationApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name="discordchannelconfiguration", url="${api.url}", configuration = OAuth2ClientConfig.class)
public interface DiscordChannelConfigurationClient extends DiscordChannelConfigurationApi {
}
