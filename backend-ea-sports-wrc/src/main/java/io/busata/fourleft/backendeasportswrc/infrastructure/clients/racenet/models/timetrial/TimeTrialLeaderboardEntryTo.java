package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.timetrial;

import lombok.Builder;

import java.util.List;

/**
 * One row of a Racenet time-trial leaderboard. Superset of the club leaderboard entry
 * ({@code ClubLeaderboardEntryTo}) — the two endpoints share the response envelope, plus TT rows
 * carry {@code splits} (per-sector times). Times come back as pre-formatted strings
 * ({@code "hh:mm:ss.fffffff"}); the fetcher stores them verbatim.
 */
@Builder
public record TimeTrialLeaderboardEntryTo(
        String ssid,
        String displayName,
        Long rank,
        String leaderboardId,
        String wrcPlayerId,
        String time,
        String differenceToFirst,
        Long nationalityID,
        String timeAccumulated,
        Long platform,
        String vehicle,
        String timePenalty,
        List<String> splits
) {
}
