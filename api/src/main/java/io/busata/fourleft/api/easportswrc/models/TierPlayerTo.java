package io.busata.fourleft.api.easportswrc.models;

/** A player: {@code playerId} is the racenet ssid. */
public record TierPlayerTo(
        String playerId,
        String displayName)
{
}
