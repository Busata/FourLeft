package io.busata.fourleft.api.acrally.models;

import java.time.LocalDateTime;
import java.util.UUID;

/** A raw car string collected from results, and the catalogue car it maps to (null until assigned). */
public record CarAliasTo(
        UUID id,
        String rawName,
        UUID carId,
        String carName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
