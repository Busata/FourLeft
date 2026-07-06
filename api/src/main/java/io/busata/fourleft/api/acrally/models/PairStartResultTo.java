package io.busata.fourleft.api.acrally.models;

/**
 * Device-authorization start response (RFC 8628 shape). The agent holds {@code deviceCode} and
 * polls with it; it shows the user {@code userCode} + {@code verificationUri}, or opens
 * {@code verificationUriComplete} (code pre-filled) directly.
 */
public record PairStartResultTo(
        String deviceCode,
        String userCode,
        String verificationUri,
        String verificationUriComplete,
        long intervalSeconds,
        long expiresInSeconds) {
}
