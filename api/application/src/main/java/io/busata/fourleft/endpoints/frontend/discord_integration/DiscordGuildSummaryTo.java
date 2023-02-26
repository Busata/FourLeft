package io.busata.fourleft.endpoints.frontend.discord_integration;

public record DiscordGuildSummaryTo (
        String id,
        String name,
        String icon,
        boolean botJoined
){
}
