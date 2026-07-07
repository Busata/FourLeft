package io.busata.fourleft.backendacrally.endpoints;

import io.busata.fourleft.backendacrally.domain.models.user.AppUser;
import io.busata.fourleft.backendacrally.domain.services.identity.SteamSignInService;
import io.busata.fourleft.backendacrally.infrastructure.properties.AcrallyProperties;
import io.busata.fourleft.backendacrally.infrastructure.security.AppUserDetails;
import io.busata.fourleft.backendacrally.infrastructure.steam.SteamOpenIdClient;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Steam OpenID 2.0 sign-in — the only way to authenticate a browser. A verified Steam
 * assertion signs the user in, provisioning the account on first visit (see
 * {@link SteamSignInService}); there is no separate registration or password.
 *
 * <p>Both endpoints are public. OpenID 2.0 has no CSRF state parameter of its own, so
 * {@code /start} plants a single-use nonce cookie and threads the same nonce through
 * {@code return_to}; {@code /return} only accepts the assertion when they match. That stops
 * login-CSRF (an attacker force-completing THEIR Steam login in a victim's browser).
 * Steam redirects back with a top-level GET, which carries the (Lax) nonce cookie.
 */
@RestController
@RequestMapping("/acrally-api/auth/steam")
@RequiredArgsConstructor
public class SteamAuthEndpoint {

    private static final String NONCE_COOKIE = "ACR_STEAM_NONCE";
    /** Post-sign-in landing path, parked while the browser round-trips through Steam. */
    private static final String REDIRECT_COOKIE = "ACR_STEAM_REDIRECT";
    private static final int NONCE_TTL_SECONDS = 300;

    private final SteamOpenIdClient steamClient;
    private final SteamSignInService signInService;
    private final SecurityContextRepository securityContextRepository;
    private final AcrallyProperties properties;

    /**
     * @param redirect optional in-app path to land on after a successful sign-in (e.g. the
     *                 pairing page with its code). Parked in a short-lived cookie for the
     *                 round-trip — return_to must stay single-valued for Steam — and only
     *                 honoured for same-origin relative paths.
     */
    @GetMapping("/start")
    public ResponseEntity<Void> start(@RequestParam(required = false) String redirect,
                                      HttpServletResponse response) {
        String nonce = UUID.randomUUID().toString();
        response.addHeader(HttpHeaders.SET_COOKIE, roundTripCookie(NONCE_COOKIE, nonce, NONCE_TTL_SECONDS).toString());
        if (isSafeRedirect(redirect)) {
            response.addHeader(HttpHeaders.SET_COOKIE,
                    roundTripCookie(REDIRECT_COOKIE, redirect, NONCE_TTL_SECONDS).toString());
        }

        String returnTo = UriComponentsBuilder.fromUriString(properties.steamReturnUrl())
                .queryParam("nonce", nonce)
                .build()
                .toUriString();
        URI location = steamClient.buildAuthenticationUrl(returnTo, properties.publicBaseUrl());
        return ResponseEntity.status(HttpStatus.FOUND).location(location).build();
    }

    @GetMapping("/return")
    public ResponseEntity<Void> handleReturn(@RequestParam Map<String, String> params,
                                             HttpServletRequest request, HttpServletResponse response) {
        // Both round-trip cookies are single-use: clear them no matter how this ends.
        response.addHeader(HttpHeaders.SET_COOKIE, roundTripCookie(NONCE_COOKIE, "", 0).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, roundTripCookie(REDIRECT_COOKIE, "", 0).toString());
        if (!nonceMatches(params.get("nonce"), request)) {
            return redirectToFrontend(properties.steam().failurePath(), "error");
        }

        Map<String, String> openidParams = new LinkedHashMap<>();
        params.forEach((key, value) -> {
            if (key.startsWith("openid.")) {
                openidParams.put(key, value);
            }
        });

        Optional<String> steamId = steamClient.verify(openidParams);
        if (steamId.isEmpty()) {
            return redirectToFrontend(properties.steam().failurePath(), "error");
        }

        AppUser user = signInService.signIn(steamId.get());
        if (user.isBanned()) {
            return redirectToFrontend(properties.steam().failurePath(), "banned");
        }

        establishSession(user, request, response);
        String redirect = cookieValue(REDIRECT_COOKIE, request);
        String landing = isSafeRedirect(redirect) ? redirect : properties.steam().successPath();
        return redirectToFrontend(landing, "signed-in");
    }

    /**
     * Only same-origin relative paths may be redirect targets — an absolute URL (or a
     * protocol-relative {@code //host}) here would be an open redirect off our origin.
     */
    private boolean isSafeRedirect(String redirect) {
        return redirect != null && redirect.startsWith("/") && !redirect.startsWith("//")
                && !redirect.contains("\\");
    }

    /** Create the cookie session for a signed-in user, rotating any pre-existing session id. */
    private void establishSession(AppUser user, HttpServletRequest request, HttpServletResponse response) {
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        AppUserDetails principal = new AppUserDetails(user);
        Authentication authentication =
                UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }

    private boolean nonceMatches(String nonceParam, HttpServletRequest request) {
        String cookie = cookieValue(NONCE_COOKIE, request);
        return nonceParam != null && !nonceParam.isBlank() && cookie != null
                && MessageDigest.isEqual(cookie.getBytes(), nonceParam.getBytes());
    }

    private String cookieValue(String name, HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private ResponseCookie roundTripCookie(String name, String value, int maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(properties.publicBaseUrl().startsWith("https"))
                .sameSite("Lax")
                .path("/acrally-api/auth/steam")
                .maxAge(maxAgeSeconds)
                .build();
    }

    private ResponseEntity<Void> redirectToFrontend(String path, String steamOutcome) {
        // Concatenate before parsing: the path may carry its own query (e.g. the pairing
        // page's ?code=...), which .path() would swallow into a path segment.
        URI target = UriComponentsBuilder.fromUriString(properties.publicBaseUrl() + path)
                .queryParam("steam", steamOutcome)
                .build()
                .toUri();
        return ResponseEntity.status(HttpStatus.FOUND).location(target).build();
    }
}
