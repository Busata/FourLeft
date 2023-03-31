package io.busata.fourleft.endpoints.club.tiers.models;

import io.busata.fourleft.domain.options.models.Vehicle;

import java.util.List;

public record CompetitionRestrictionCreateTo(
        List<Vehicle> vehicles
) {
}
