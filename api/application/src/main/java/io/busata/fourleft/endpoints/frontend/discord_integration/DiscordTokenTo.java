package io.busata.fourleft.endpoints.frontend.discord_integration;

public record DiscordTokenTo(String access_token, String token_type, int expires_in, String refresh_token, String scope) {

}
