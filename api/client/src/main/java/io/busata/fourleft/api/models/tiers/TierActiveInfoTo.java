package io.busata.fourleft.api.models.tiers;

import io.busata.fourleft.api.models.views.VehicleTo;

import java.util.List;

public record TierActiveInfoTo(
        String eventId,
        String challengeId,
        String country,
        List<String> stageNames,
        String vehicleClass,
        List<VehicleTo> vehicles
) {
}
