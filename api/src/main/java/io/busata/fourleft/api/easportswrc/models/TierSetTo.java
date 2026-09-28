package io.busata.fourleft.api.easportswrc.models;

import java.util.List;
import java.util.UUID;

/** A tier set as shown on its edit page; tiers are ordered top tier first. */
public record TierSetTo(
        UUID id,
        String name,
        List<TierTo> tiers)
{
}
