package io.busata.fourleft.endpoints.frontend.discord_integration.feign;

public record DiscordUserTo(String id, String username, boolean bot, String email) {
}
