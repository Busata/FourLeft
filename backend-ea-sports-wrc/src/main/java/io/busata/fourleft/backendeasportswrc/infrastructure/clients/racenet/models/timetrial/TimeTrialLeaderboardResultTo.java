package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.timetrial;

import lombok.Builder;

import java.util.List;

/**
 * Racenet time-trial leaderboard response
 * ({@code GET /api/wrc2023Stats/leaderboard/{routeId}/{vehicleClassId}/{surface}}).
 *
 * <p>{@code totalEntrantCount} is the popularity signal the probe reads; {@code next} is the
 * pagination cursor the fetcher walks; {@code entries} is the page of rows (empty/ignored by the
 * probe, which requests a single result). Unmapped fields ({@code percentile}, …) are ignored.
 */
@Builder
public record TimeTrialLeaderboardResultTo(
        String next,
        String previous,
        Long totalEntrantCount,
        List<TimeTrialLeaderboardEntryTo> entries
) {
}
