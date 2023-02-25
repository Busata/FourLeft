package io.busata.fourleft.endpoints.frontend.discord_integration;

import io.busata.fourleft.api.Routes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DiscordIntegrationEndpoint {
    private final DiscordIntegrationService discordIntegrationService;
    private final DiscordApiClient discordApiClient;

    @GetMapping(Routes.DISCORD_AUTHENTICATION_STATUS)
    public DiscordAuthenticationStatusTo getAuthenticationStatus() {
        return new DiscordAuthenticationStatusTo(
                this.discordIntegrationService.isAuthenticated()
        );
    }

    @PostMapping(Routes.DISCORD_INTEGRATION_AUTH)
    public void authenticate(@RequestBody String code) {
        this.discordIntegrationService.fetchAndStoreAccessToken(code);
    }

    @GetMapping(Routes.DISCORD_GUILDS)
    @DiscordAuthenticated
    public List<DiscordGuildTo> getGuilds() {
        return this.discordApiClient.getGuilds();
    }

}
