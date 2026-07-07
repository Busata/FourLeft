package io.busata.fourleft.api.acrally.models;

import java.util.UUID;

/** Assign a car alias to a catalogue car ({@code carId} null clears the assignment). */
public record UpdateCarAliasRequestTo(UUID carId) {
}
