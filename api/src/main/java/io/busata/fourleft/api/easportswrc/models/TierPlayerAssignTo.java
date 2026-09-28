package io.busata.fourleft.api.easportswrc.models;

import java.util.UUID;

/** Puts a player (racenet ssid + display name) in a tier, moving them out of any other tier of the set. */
public record TierPlayerAssignTo(
        UUID tierId,
        String playerId,
        String displayName)
{
}
