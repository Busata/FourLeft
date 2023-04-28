package io.busata.fourleft.endpoints.discord.integration;

import io.busata.fourleft.api.models.discord.DiscordGuildSummaryTo;
import io.busata.fourleft.api.models.discord.DiscordGuildTo;
import io.busata.fourleft.helpers.Factory;

@Factory
public class DiscordGuildSummaryToFactory {

    public DiscordGuildSummaryTo create(DiscordGuildTo guild, boolean botJoined) {
        return new DiscordGuildSummaryTo(
                guild.id(),
                guild.name(),
                guild.icon(),
                botJoined);
    }
}
