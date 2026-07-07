package io.busata.fourleft.api.acrally.models;

import java.time.LocalDateTime;
import java.util.UUID;

/** A collected variant: its raw key, assigned display name, and the stage/location it resolves to. */
public record VariantTo(
        UUID id,
        String rawName,
        String displayName,
        UUID stageId,
        String stageName,
        String locationName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
