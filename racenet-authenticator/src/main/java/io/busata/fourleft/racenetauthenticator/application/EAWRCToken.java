package io.busata.fourleft.racenetauthenticator.application;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EAWRCToken(
        @JsonProperty(value="access_token") String accessToken,
        @JsonProperty(value="token_type") String tokenType,
        @JsonProperty(value="expires_in") Long expiresIn,
        @JsonProperty(value="refresh_token") String refreshToken,
        @JsonProperty(value="id_token") String idToken
) {
}
