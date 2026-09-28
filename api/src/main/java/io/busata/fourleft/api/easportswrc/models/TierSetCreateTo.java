package io.busata.fourleft.api.easportswrc.models;

/** Creates a tier set from Discord; {@code guildId} scopes it for later edit links. */
public record TierSetCreateTo(
        String name,
        Long guildId,
        String discordId)
{
}
