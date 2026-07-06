package io.busata.fourleft.api.acrally.models;

import java.time.LocalDateTime;
import java.util.UUID;

/** A driving session in the user's own dashboard. */
public record MySessionTo(
        UUID id,
        String driver,
        String car,
        String stage,
        String track,
        String status,
        Long startedAtMs,
        Integer currentMs,
        LocalDateTime lastHeartbeatAt,
        LocalDateTime createdAt) {
}
