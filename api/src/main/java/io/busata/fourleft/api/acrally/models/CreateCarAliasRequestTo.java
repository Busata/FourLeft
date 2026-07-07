package io.busata.fourleft.api.acrally.models;

import java.util.UUID;

/** Manually add a car alias: the exact raw string the game reports, optionally assigned to a car. */
public record CreateCarAliasRequestTo(String rawName, UUID carId) {
}
