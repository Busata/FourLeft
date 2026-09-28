package io.busata.fourleft.api.easportswrc.models;

import java.util.List;
import java.util.UUID;

/** The tiers of a set in their new order, top tier first. */
public record TierOrderTo(
        List<UUID> tierIds)
{
}
