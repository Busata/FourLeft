package io.busata.fourleft.backendacrally.infrastructure.steam;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimal Steam OpenID 2.0 client. Steam speaks OpenID 2.0 (not OAuth2/OIDC), so this is
 * a hand-rolled two-step flow: send the user to Steam with a {@code checkid_setup}
 * request, then verify the assertion Steam bounces back via a {@code check_authentication}
 * POST. A verified assertion yields the 17-digit steamID64.
 */
@Component
public class SteamOpenIdClient {

    private static final String STEAM_LOGIN = "https://steamcommunity.com/openid/login";
    private static final String OPENID_NS = "http://specs.openid.net/auth/2.0";
    private static final String IDENTIFIER_SELECT = "http://specs.openid.net/auth/2.0/identifier_select";
    // claimed_id comes back as https://steamcommunity.com/openid/id/<steamID64>
    private static final Pattern CLAIMED_ID = Pattern.compile("^https?://steamcommunity\\.com/openid/id/(\\d{17})$");

    private final RestClient restClient = RestClient.create();

    /**
     * The URL to redirect the user's browser to so they authenticate with Steam.
     * {@code realm} must cover {@code returnTo} (same origin).
     */
    public URI buildAuthenticationUrl(String returnTo, String realm) {
        return UriComponentsBuilder.fromUriString(STEAM_LOGIN)
                .queryParam("openid.ns", OPENID_NS)
                .queryParam("openid.mode", "checkid_setup")
                .queryParam("openid.return_to", returnTo)
                .queryParam("openid.realm", realm)
                .queryParam("openid.identity", IDENTIFIER_SELECT)
                .queryParam("openid.claimed_id", IDENTIFIER_SELECT)
                .build()
                .toUri();
    }

    /**
     * Verifies the openid.* parameters Steam returned. Returns the steamID64 only when
     * Steam confirms the assertion is genuine — this is the step a forged callback fails.
     *
     * @param openidParams every {@code openid.*} query parameter from the return request
     */
    public Optional<String> verify(Map<String, String> openidParams) {
        if (!"id_res".equals(openidParams.get("openid.mode"))) {
            return Optional.empty();
        }
        String claimedId = openidParams.get("openid.claimed_id");
        if (claimedId == null) {
            return Optional.empty();
        }
        Matcher matcher = CLAIMED_ID.matcher(claimedId);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        String steamId = matcher.group(1);

        // Echo the assertion back verbatim, flipping only the mode, and ask Steam to
        // confirm it signed it. Anything less lets a caller forge a claimed_id.
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        openidParams.forEach(form::add);
        form.set("openid.mode", "check_authentication");

        String response = restClient.post()
                .uri(STEAM_LOGIN)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(String.class);

        boolean valid = response != null && response.lines().anyMatch("is_valid:true"::equals);
        return valid ? Optional.of(steamId) : Optional.empty();
    }
}
