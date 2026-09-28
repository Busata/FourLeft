package io.busata.fourleft.api.easportswrc.models;

import java.util.List;
import java.util.UUID;

/** One tier; players are sorted by display name. */
public record TierTo(
        UUID id,
        String label,
        List<TierPlayerTo> players)
{
}
