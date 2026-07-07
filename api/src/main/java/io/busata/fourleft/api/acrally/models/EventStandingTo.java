package io.busata.fourleft.api.acrally.models;

import java.util.UUID;

/**
 * A driver's overall standing in an event: their summed best across completed stages, ranked so that
 * completing more stages outranks a faster partial ({@code stagesCompleted} desc, then {@code totalMs}).
 */
public record EventStandingTo(
        int rank,
        UUID userId,
        String driver,
        long totalMs,
        int stagesCompleted) {
}
