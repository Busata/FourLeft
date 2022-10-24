package io.busata.fourleft.api.models.tiers;

import java.util.List;

public record TierInfoTo(
        String tierName,
        List<String> allowedVehicles
) {
}
