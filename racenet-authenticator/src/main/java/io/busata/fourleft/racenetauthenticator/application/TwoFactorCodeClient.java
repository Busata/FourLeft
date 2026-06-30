package io.busata.fourleft.racenetauthenticator.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;

/**
 * Talks to the racenet-2fa-relay service, which captures the EA 2FA "Security Code"
 * e-mails (forwarded via Postmark) and serves the latest one on a token-protected endpoint.
 *
 * Usage during a login: {@link #clear()} just before triggering the code e-mail so a stale
 * code can never be consumed, then {@link #waitForCode()} to poll until the new code arrives.
 */
@Component
@Slf4j
public class TwoFactorCodeClient {

    private final WebClient client;
    private final Duration pollTimeout;
    private final Duration pollInterval;

    public TwoFactorCodeClient(
            @Value("${racenet-authenticator.twofactor.base-url:http://racenet-2fa-relay:8085}") String baseUrl,
            @Value("${racenet-authenticator.twofactor.api-token:}") String apiToken,
            @Value("${racenet-authenticator.twofactor.poll-timeout-seconds:120}") long pollTimeoutSeconds,
            @Value("${racenet-authenticator.twofactor.poll-interval-seconds:2}") long pollIntervalSeconds
    ) {
        this.client = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiToken)
                .build();
        this.pollTimeout = Duration.ofSeconds(pollTimeoutSeconds);
        this.pollInterval = Duration.ofSeconds(pollIntervalSeconds);
    }

    /** Clears any code currently held by the relay. Call before triggering a new 2FA e-mail. */
    public void clear() {
        client.post()
                .uri("/code/clear")
                .retrieve()
                .toBodilessEntity()
                .block();
        log.info("Cleared any existing 2FA code on the relay.");
    }

    /**
     * Polls the relay until a code is available or the timeout elapses.
     *
     * @return the 6-digit code
     * @throws IllegalStateException if no code arrives within the configured timeout
     */
    public String waitForCode() throws InterruptedException {
        long deadline = System.currentTimeMillis() + pollTimeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            String code = fetchCode();
            if (code != null) {
                return code;
            }
            Thread.sleep(pollInterval.toMillis());
        }
        throw new IllegalStateException(
                "Timed out waiting for 2FA code from relay after " + pollTimeout.toSeconds() + "s");
    }

    /** Returns the current code, or {@code null} if none is available yet (relay returns 404). */
    private String fetchCode() {
        try {
            CodeResponse response = client.get()
                    .uri("/code")
                    .retrieve()
                    .bodyToMono(CodeResponse.class)
                    .block();
            return response != null ? response.code() : null;
        } catch (WebClientResponseException.NotFound notFound) {
            return null; // no code yet
        } catch (WebClientResponseException ex) {
            // Transient relay error (e.g. 401/5xx); keep polling rather than aborting the login.
            log.warn("Unexpected response from 2FA relay while polling: {}", ex.getStatusCode());
            return null;
        }
    }

    private record CodeResponse(String code) {
    }
}
