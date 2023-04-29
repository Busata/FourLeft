package io.busata.fourleft.infrastructure.clients.discord.auth;

import feign.Headers;
import io.busata.fourleft.api.models.discord.DiscordTokenTo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "discordoauthclient", url = "https://discord.com/api/")
public interface DiscordOauth2Client {

    @PostMapping(path="/oauth2/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Headers("Content-Type: application/x-www-form-urlencoded")
    DiscordTokenTo requestToken(@RequestBody String tokenData);

}
