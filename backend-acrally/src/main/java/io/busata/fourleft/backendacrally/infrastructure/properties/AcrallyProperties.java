package io.busata.fourleft.backendacrally.infrastructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.ZoneId;

/**
 * @param publicBaseUrl the origin the browser (and Steam) use to reach this service —
 *                      the OpenID realm and return_to are built from it. Prod:
 *                      {@code https://fourleft.io} (via the reverse proxy); local:
 *                      {@code http://localhost:8085}.
 * @param steam         where to send the browser after the Steam round-trip.
 * @param timeZone      the wall-clock zone championship schedules are authored in. Owners pick a
 *                      naive local time in the browser ({@code datetime-local}); this is the zone the
 *                      server interprets it (and "now") in when deciding whether an event is open.
 *                      Defaults to the community's zone; set {@code acrally.time-zone} to override.
 */
@ConfigurationProperties(prefix = "acrally")
public record AcrallyProperties(String publicBaseUrl, Steam steam, String timeZone) {

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Europe/Brussels");

    /** The configured scheduling zone, falling back to the community default when unset. */
    public ZoneId zoneId() {
        if (timeZone == null || timeZone.isBlank()) {
            return DEFAULT_ZONE;
        }
        return ZoneId.of(timeZone.trim());
    }

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
