package io.busata.fourleft.api.easportswrc.models;

import java.util.UUID;

/** An edit link for a tier set; the page lives at /easportswrc/tiers/{linkId}. */
public record TierSetLinkTo(
        UUID linkId,
        String name)
{
}
