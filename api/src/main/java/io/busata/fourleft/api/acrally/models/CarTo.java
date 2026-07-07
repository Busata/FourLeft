package io.busata.fourleft.api.acrally.models;

import java.time.LocalDateTime;
import java.util.UUID;

/** A rally car in the reference catalogue. */
public record CarTo(
        UUID id,
        String name,
        Integer year,
        String groupName,
        String className,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
