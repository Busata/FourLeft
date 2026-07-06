package io.busata.fourleft.api.acrally.models;

import java.time.LocalDateTime;
import java.util.UUID;

/** A recorded stage result in the user's own dashboard. */
public record MyResultTo(
        UUID id,
        String stage,
        String car,
        String driver,
        int rawMs,
        int penaltyMs,
        int totalMs,
        LocalDateTime recordedAt) {
}
