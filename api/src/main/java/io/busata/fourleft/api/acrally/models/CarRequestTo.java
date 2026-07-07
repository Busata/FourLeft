package io.busata.fourleft.api.acrally.models;

/** Create/update payload for a car. */
public record CarRequestTo(String name, Integer year, String groupName, String className) {
}
