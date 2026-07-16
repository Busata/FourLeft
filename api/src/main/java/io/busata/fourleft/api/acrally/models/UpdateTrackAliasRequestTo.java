package io.busata.fourleft.api.acrally.models;

import java.util.UUID;

/** Assign a track alias to a variant ({@code variantId} null clears the assignment). */
public record UpdateTrackAliasRequestTo(UUID variantId) {
}
