package io.busata.fourleft.api.easportswrc.models;

/** Asks for a new edit link to the guild's tier set with this name. */
public record TierSetLinkRequestTo(
        String name,
        Long guildId,
        String discordId)
{
}
