package io.busata.fourleft.api.acrally.models;

import java.time.LocalDateTime;
import java.util.UUID;

/** A telemetry track string collected from sessions, and the variant it maps to (null until assigned). */
public record TrackAliasTo(
        UUID id,
        String rawName,
        UUID variantId,
        String variantLabel,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
