package io.busata.fourleftdiscord.client;

import io.busata.fourleft.api.DiscordChannelConfigurationApi;
import io.busata.fourleft.api.FourLeftApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name="fourleftapi", url="${api.url}", configuration = OAuth2ClientConfig.class)
public interface FourLeftClient extends FourLeftApi {
}
