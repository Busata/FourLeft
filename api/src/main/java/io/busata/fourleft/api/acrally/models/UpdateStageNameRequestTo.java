package io.busata.fourleft.api.acrally.models;

/** Assigns (or clears, when blank) the readable display name for a catalogued stage. */
public record UpdateStageNameRequestTo(String displayName) {
}
