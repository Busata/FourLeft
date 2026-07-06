package io.busata.fourleft.api.acrally.models;

/**
 * Poll response. {@code status} is one of {@code pending|approved|denied|expired|consumed};
 * {@code apiKey} is present only on {@code approved} (returned once — the agent must store it).
 */
public record PairTokenResultTo(String status, String apiKey, String label) {
}
