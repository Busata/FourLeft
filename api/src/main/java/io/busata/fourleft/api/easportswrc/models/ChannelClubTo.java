package io.busata.fourleft.api.easportswrc.models;

/** A club tracked by a channel; {@code label} names its class or tier ("Rally2", "JRC 1") and may be null. */
public record ChannelClubTo(
        String clubId,
        String label)
{
}
