package io.busata.fourleft.backendacrally.endpoints;

import io.busata.fourleft.backendacrally.domain.services.identity.SteamLinkService;
import io.busata.fourleft.backendacrally.domain.services.identity.SteamProfileService;
import io.busata.fourleft.backendacrally.infrastructure.properties.AcrallyProperties;
import io.busata.fourleft.backendacrally.infrastructure.security.AppUserDetails;
import io.busata.fourleft.backendacrally.infrastructure.steam.SteamOpenIdClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Steam OpenID 2.0 link flow. Both endpoints run on an authenticated session — the point
 * is to attach a verified Steam id to the logged-in account, so the user must be logged in
 * first. Steam redirects the browser back with a top-level GET, which carries the (Lax)
 * session cookie, so the return lands authenticated too.
 */
@RestController
@RequestMapping("/acrally-api/auth/steam")
@RequiredArgsConstructor
public class SteamAuthEndpoint {

    private final SteamOpenIdClient steamClient;
    private final SteamLinkService steamLinkService;
    private final SteamProfileService steamProfileService;
    private final AcrallyProperties properties;

    @GetMapping("/start")
    public ResponseEntity<Void> start(@AuthenticationPrincipal AppUserDetails principal) {
        requireLogin(principal);
        URI redirect = steamClient.buildAuthenticationUrl(properties.steamReturnUrl(), properties.publicBaseUrl());
        return ResponseEntity.status(HttpStatus.FOUND).location(redirect).build();
    }

    @GetMapping("/return")
    public ResponseEntity<Void> handleReturn(@AuthenticationPrincipal AppUserDetails principal,
                                             @RequestParam Map<String, String> params) {
        requireLogin(principal);

        Map<String, String> openidParams = new LinkedHashMap<>();
        params.forEach((key, value) -> {
            if (key.startsWith("openid.")) {
                openidParams.put(key, value);
            }
        });

        Optional<String> steamId = steamClient.verify(openidParams);
        if (steamId.isEmpty()) {
            return redirectToFrontend(properties.steam().failurePath(), "steam", "error");
        }

        try {
            steamLinkService.linkSteam(principal.getId(), steamId.get());
        } catch (ResponseStatusException conflict) {
            return redirectToFrontend(properties.steam().failurePath(), "steam", "error");
        }
        // Populate persona/avatar/bans synchronously so the account page shows them right away.
        steamProfileService.refresh(steamId.get());
        return redirectToFrontend(properties.steam().successPath(), "steam", "linked");
    }

    private void requireLogin(AppUserDetails principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    private ResponseEntity<Void> redirectToFrontend(String path, String queryKey, String queryValue) {
        URI target = UriComponentsBuilder.fromUriString(properties.publicBaseUrl())
                .path(path)
                .queryParam(queryKey, queryValue)
                .build()
                .toUri();
        return ResponseEntity.status(HttpStatus.FOUND).location(target).build();
    }
}
