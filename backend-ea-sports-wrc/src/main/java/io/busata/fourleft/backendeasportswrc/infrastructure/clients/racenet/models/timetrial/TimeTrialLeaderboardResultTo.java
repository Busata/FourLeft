package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.timetrial;

import lombok.Builder;

/**
 * Racenet time-trial leaderboard response
 * ({@code GET /api/wrc2023Stats/leaderboard/{routeId}/{vehicleClassId}/{surface}}).
 *
 * <p>Only the fields the probe needs are mapped ({@code totalEntrantCount} is the popularity signal;
 * {@code next} is the pagination cursor the future fetch worker will use). Unmapped fields
 * ({@code entries}, {@code percentile}, …) are ignored.
 */
@Builder
public record TimeTrialLeaderboardResultTo(
        String next,
        String previous,
        Long totalEntrantCount
) {
}
