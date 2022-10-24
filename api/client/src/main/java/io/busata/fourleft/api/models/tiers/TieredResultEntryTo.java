package io.busata.fourleft.api.models.tiers;

import io.busata.fourleft.api.models.ResultEntryTo;

public record TieredResultEntryTo(
        String tierName,
        boolean usesValidVehicle,
        ResultEntryTo entry) {
}
