package io.busata.fourleft.backendacrally.infrastructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param publicBaseUrl the origin the browser (and Steam) use to reach this service —
 *                      the OpenID realm and return_to are built from it. Prod:
 *                      {@code https://fourleft.io} (via the reverse proxy); local:
 *                      {@code http://localhost:8085}.
 * @param steam         where to send the browser after the Steam round-trip.
 */
@ConfigurationProperties(prefix = "acrally")
public record AcrallyProperties(String publicBaseUrl, Steam steam) {

    /**
     * @param webApiKey Steam Web API key (steamcommunity.com/dev/apikey). Blank disables the
     *                  profile fetch entirely — linking still works, just without persona/avatar.
     */
    public record Steam(String successPath, String failurePath, String webApiKey) {
    }

    public String steamReturnUrl() {
        return publicBaseUrl + "/acrally-api/auth/steam/return";
    }
}
