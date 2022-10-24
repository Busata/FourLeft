package io.busata.fourleft.api.models.tiers;

import java.util.List;

public record TierResultTo (
        List<TierInfoTo> tierInfo,
        ClubEventInfoTo eventInfo,
        List<TieredResultEntryTo> entries
) {
}
