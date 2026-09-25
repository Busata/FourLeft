package io.busata.fourleft.backendeasportswrc.application.discord.results;

import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * An overall classification across several clubs' boards for the same event, by accumulated time —
 * exactly how racenet ranks a single board, DNFs included (their penalty time places them). Each club's own
 * rank and gap only hold within its board, so both are recomputed against the overall leader.
 */
public final class MergedRanking {

    public record Ranked(ClubLeaderboardEntry entry, long rank, Duration delta) {
    }

    private final List<Ranked> ranked;
    private final Map<ClubLeaderboardEntry, Ranked> byEntry = new IdentityHashMap<>();

    private MergedRanking(List<Ranked> ranked) {
        this.ranked = ranked;
        ranked.forEach(r -> byEntry.put(r.entry(), r));
    }

    public static MergedRanking of(Collection<ClubLeaderboardEntry> entries) {
        List<ClubLeaderboardEntry> sorted = entries.stream()
                .sorted(Comparator.comparing(ClubLeaderboardEntry::getTimeAccumulated))
                .toList();

        List<Ranked> ranked = new ArrayList<>();
        Duration leaderTime = sorted.isEmpty() ? Duration.ZERO : sorted.get(0).getTimeAccumulated();
        for (ClubLeaderboardEntry entry : sorted) {
            ranked.add(new Ranked(entry, ranked.size() + 1, entry.getTimeAccumulated().minus(leaderTime)));
        }
        return new MergedRanking(ranked);
    }

    public List<Ranked> ranked() {
        return ranked;
    }

    public List<ClubLeaderboardEntry> entries() {
        return ranked.stream().map(Ranked::entry).toList();
    }

    public long rankOf(ClubLeaderboardEntry entry) {
        return byEntry.get(entry).rank();
    }

    public Duration deltaOf(ClubLeaderboardEntry entry) {
        return byEntry.get(entry).delta();
    }
}
