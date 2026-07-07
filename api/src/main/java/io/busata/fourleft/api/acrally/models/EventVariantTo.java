package io.busata.fourleft.api.acrally.models;

import java.util.UUID;

/** A variant selected into an event, at its running-order position, with resolved labels. */
public record EventVariantTo(
        UUID variantId,
        int position,
        String label,
        String stageName,
        String locationName) {
}
