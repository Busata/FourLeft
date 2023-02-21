package io.busata.fourleft.endpoints.frontend.discord_integration;

import io.busata.fourleft.api.Routes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DiscordIntegrationController {

    private final DiscordIntegrationService discordIntegrationService;
    private final DiscordApiClient discordApiClient;
    private final DiscordOauth2Client discordOauth2Client;

    private final DiscordIntegrationConfigurationProperties discordIntegrationConfigurationProperties;

    @GetMapping(Routes.DISCORD_CALLBACK)
    @ResponseStatus(HttpStatus.FOUND)
    //Utility function to redirect to an url with # in it.
    public void discordCallback(@RequestParam String code, HttpServletResponse response) {
        final var url = "http://localhost:4200/#/discord_callback?code=" + code;
        response.setHeader("Location", url);
    }


    @PostMapping(Routes.DISCORD_INTEGRATION_AUTH)
    public void authenticate(@RequestBody String code) {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();

        this.discordIntegrationService.setToken(authentication.getName(), getAccessToken(code));
    }

    private DiscordTokenTo getAccessToken(String code) {
        String body = Map.of(
                        "client_id", discordIntegrationConfigurationProperties.getClientId(),
                        "client_secret", discordIntegrationConfigurationProperties.getClientSecret(),
                        "code", code,
                        "grant_type", "authorization_code",
                        "redirect_uri", discordIntegrationConfigurationProperties.getRedirectUri(),
                        "scope", "identify guilds"
                ).entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .reduce((s, s2) -> s + "&" + s2).orElseThrow();

        return this.discordOauth2Client.getToken(body);
    }

    @GetMapping(Routes.DISCORD_GUILDS)
    public List<DiscordGuildTo> getGuilds() {
        return this.discordApiClient.getGuilds();
    }

}
