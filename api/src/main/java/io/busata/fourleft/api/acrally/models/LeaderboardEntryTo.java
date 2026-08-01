package io.busata.fourleft.api.acrally.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** One driver's best time on a stage board, at its rank (1-based, fastest first). */
public record LeaderboardEntryTo(
        int rank,
        UUID userId,
        String driver,
        String carName,
        int rawMs,
        int penaltyMs,
        int totalMs,
        /** Cumulative checkpoint splits in ms, finish included; empty when unavailable. */
        List<Integer> checkpointsMs,
        LocalDateTime recordedAt) {
}
