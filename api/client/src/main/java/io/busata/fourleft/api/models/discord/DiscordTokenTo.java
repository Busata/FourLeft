package io.busata.fourleft.api.models.discord;

public record DiscordTokenTo(String access_token, String token_type, int expires_in, String refresh_token, String scope) {

}
