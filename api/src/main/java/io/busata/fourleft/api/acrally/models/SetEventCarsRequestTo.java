package io.busata.fourleft.api.acrally.models;

import java.util.List;
import java.util.UUID;

/** Replaces the set of cars permitted in an event. */
public record SetEventCarsRequestTo(
        List<UUID> carIds) {
}
