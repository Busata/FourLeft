package io.busata.fourleft.backendeasportswrc.application.discord.results;

import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A MIXED channel counts one run per driver: a driver entering several of its classes keeps only the entry
 * first seen on a board. Racenet gives no run timestamp, so "first" is the sync that first saw it; entries
 * from before first-seen tracking (null) count as earliest, and ties go to channel order (primary first).
 */
public final class FirstRuns {

    private static final Comparator<ClubLeaderboardEntry> FIRST_SEEN =
            Comparator.comparing(ClubLeaderboardEntry::getFirstSeenAt, Comparator.nullsFirst(Comparator.<LocalDateTime>naturalOrder()));

    private FirstRuns() {
    }

    /** {@code entries} in channel order; the result keeps that order. */
    public static List<ClubLeaderboardEntry> of(List<ClubLeaderboardEntry> entries) {
        Map<String, ClubLeaderboardEntry> first = new LinkedHashMap<>();
        for (ClubLeaderboardEntry entry : entries) {
            first.merge(entry.getPlayerKey(), entry, (kept, other) -> FIRST_SEEN.compare(other, kept) < 0 ? other : kept);
        }
        return entries.stream().filter(entry -> first.get(entry.getPlayerKey()) == entry).toList();
    }
}
