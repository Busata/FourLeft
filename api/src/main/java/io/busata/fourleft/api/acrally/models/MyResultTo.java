package io.busata.fourleft.api.acrally.models;

import java.time.LocalDateTime;
import java.util.List;
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
        /** Cumulative checkpoint splits in ms, finish included; empty when the agent had none. */
        List<Integer> checkpointsMs,
        LocalDateTime recordedAt) {
}
