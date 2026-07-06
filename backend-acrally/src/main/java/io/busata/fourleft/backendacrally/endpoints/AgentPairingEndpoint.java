package io.busata.fourleft.backendacrally.endpoints;

import io.busata.fourleft.api.acrally.models.PairApproveRequestTo;
import io.busata.fourleft.api.acrally.models.PairLookupResultTo;
import io.busata.fourleft.api.acrally.models.PairStartRequestTo;
import io.busata.fourleft.api.acrally.models.PairStartResultTo;
import io.busata.fourleft.api.acrally.models.PairTokenRequestTo;
import io.busata.fourleft.api.acrally.models.PairTokenResultTo;
import io.busata.fourleft.backendacrally.domain.models.agent.DevicePairing;
import io.busata.fourleft.backendacrally.domain.services.agent.DevicePairingService;
import io.busata.fourleft.backendacrally.infrastructure.properties.AcrallyProperties;
import io.busata.fourleft.backendacrally.infrastructure.security.AppUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/acrally-api/agent/pair")
@RequiredArgsConstructor
public class AgentPairingEndpoint {

    private static final long POLL_INTERVAL_SECONDS = 5;

    private final DevicePairingService pairingService;
    private final AcrallyProperties properties;

    // --- agent-facing (no session; the device_code is the secret) ---

    @PostMapping("/start")
    public PairStartResultTo start(@RequestBody(required = false) PairStartRequestTo request) {
        String label = request == null ? null : request.label();
        DevicePairingService.Started started = pairingService.start(label);

        String verificationUri = properties.publicBaseUrl() + "/acrally/link";
        String complete = verificationUri + "?code="
                + URLEncoder.encode(started.userCode(), StandardCharsets.UTF_8);

        return new PairStartResultTo(
                started.deviceCode(),
                started.userCode(),
                verificationUri,
                complete,
                POLL_INTERVAL_SECONDS,
                started.expiresInSeconds());
    }

    @PostMapping("/token")
    public PairTokenResultTo token(@RequestBody PairTokenRequestTo request) {
        DevicePairingService.PollResult result = pairingService.poll(request.deviceCode());
        return new PairTokenResultTo(
                result.status().name().toLowerCase(),
                result.apiKey(),
                result.label());
    }

    // --- browser-facing (authenticated session) ---

    @GetMapping("/lookup")
    public PairLookupResultTo lookup(@RequestParam("user_code") String userCode,
                                     @AuthenticationPrincipal AppUserDetails principal) {
        requireLogin(principal);
        DevicePairing pairing = pairingService.lookupPending(userCode);
        return new PairLookupResultTo(pairing.getUserCode(), pairing.getLabel(), pairing.getExpiresAt());
    }

    @PostMapping("/approve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void approve(@RequestBody PairApproveRequestTo request,
                        @AuthenticationPrincipal AppUserDetails principal) {
        requireLogin(principal);
        pairingService.approve(request.userCode(), principal.getId());
    }

    @PostMapping("/deny")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deny(@RequestBody PairApproveRequestTo request,
                     @AuthenticationPrincipal AppUserDetails principal) {
        requireLogin(principal);
        pairingService.deny(request.userCode());
    }

    private void requireLogin(AppUserDetails principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
}
