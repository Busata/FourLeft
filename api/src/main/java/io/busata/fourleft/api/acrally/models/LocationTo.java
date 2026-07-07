package io.busata.fourleft.api.acrally.models;

import java.time.LocalDateTime;
import java.util.UUID;

/** A rally location, with the number of stages assigned to it (which blocks deletion when > 0). */
public record LocationTo(
        UUID id,
        String name,
        String nation,
        long stageCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
