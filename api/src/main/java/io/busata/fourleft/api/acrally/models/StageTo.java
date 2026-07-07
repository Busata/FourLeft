package io.busata.fourleft.api.acrally.models;

import java.time.LocalDateTime;
import java.util.UUID;

/** A stage, with its (optional) location and the number of variants assigned (which blocks deletion when > 0). */
public record StageTo(
        UUID id,
        String name,
        UUID locationId,
        String locationName,
        long variantCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
