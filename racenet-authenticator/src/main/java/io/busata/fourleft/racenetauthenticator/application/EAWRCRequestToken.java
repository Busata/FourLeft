package io.busata.fourleft.racenetauthenticator.application;

public record EAWRCRequestToken(
        String authCode,
        String clientId,
        String codeVerifier,
        String grantType,
        String redirectUri,
        String refreshToken
) {
}
