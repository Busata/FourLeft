package io.busata.fourleft.api.acrally.models;

import java.time.LocalDateTime;
import java.util.UUID;

/** A catalogued stage: its raw identifier and the readable display name assigned to it (if any). */
public record StageNameTo(
        UUID id,
        String rawName,
        String displayName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
