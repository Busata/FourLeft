package io.busata.fourleft.backendeasportswrc.application.discord.results;

import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;
import io.busata.fourleft.common.ClassLockMode;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A MIXED channel locks each driver to their home class: the club of their first run in the championship —
 * the earliest event they entered, and within it the run {@link FirstRuns} keeps. Keyed by player key.
 * With {@link ClassLockMode#OFF} (and for single-club channels) nobody has a home and nothing changes.
 */
public final class HomeClasses {

    /** One event's entries in channel order, each with the club it came from. */
    public record EventEntries(List<ClubLeaderboardEntry> entries, Map<ClubLeaderboardEntry, String> clubs) {
    }

    private static final HomeClasses NONE = new HomeClasses(ClassLockMode.OFF, Map.of());

    private final ClassLockMode mode;
    private final Map<String, String> homes;

    private HomeClasses(ClassLockMode mode, Map<String, String> homes) {
        this.mode = mode;
        this.homes = homes;
    }

    public static HomeClasses none() {
        return NONE;
    }

    /** {@code events} in running order. */
    public static HomeClasses of(ClassLockMode mode, List<EventEntries> events) {
        if (mode == ClassLockMode.OFF) {
            return NONE;
        }
        Map<String, String> homes = new HashMap<>();
        for (EventEntries event : events) {
            for (ClubLeaderboardEntry first : FirstRuns.of(event.entries())) {
                homes.putIfAbsent(first.getPlayerKey(), event.clubs().get(first));
            }
        }
        return new HomeClasses(mode, homes);
    }

    /** These homes plus those of drivers first seen in {@code laterEvents} (in running order). */
    public HomeClasses then(List<EventEntries> laterEvents) {
        if (mode == ClassLockMode.OFF || laterEvents.isEmpty()) {
            return this;
        }
        HomeClasses later = of(mode, laterEvents);
        Map<String, String> combined = new HashMap<>(later.homes);
        combined.putAll(homes);
        return new HomeClasses(mode, combined);
    }

    public ClassLockMode mode() {
        return mode;
    }

    /** The club the driver is locked to; null when unlocked or not seen yet. */
    public String homeOf(ClubLeaderboardEntry entry) {
        return homes.get(entry.getPlayerKey());
    }

    /** Whether the entry, a run in {@code clubId}, lies outside its driver's home class. */
    public boolean isOffClass(String clubId, ClubLeaderboardEntry entry) {
        String home = homeOf(entry);
        return home != null && !home.equals(clubId);
    }

    /** Player keys locked to another club than {@code clubId}, to leave out of that club's standings. */
    public Set<String> lockedOut(String clubId) {
        return homes.entrySet().stream()
                .filter(home -> !home.getValue().equals(clubId))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * The one run per driver an event shows, from its entries in channel order: the home-class run when
     * there is one, else — under WARN — the driver's first run, flagged by {@link #isOffClass}. Under
     * EXCLUDE a driver without a home-class run is left out.
     */
    public List<ClubLeaderboardEntry> select(List<ClubLeaderboardEntry> entries, Function<ClubLeaderboardEntry, String> clubOf) {
        if (mode == ClassLockMode.OFF) {
            return FirstRuns.of(entries);
        }
        Map<ClubLeaderboardEntry, Boolean> offClass = new IdentityHashMap<>();
        entries.forEach(entry -> offClass.put(entry, isOffClass(clubOf.apply(entry), entry)));

        Set<String> withHomeRun = entries.stream()
                .filter(entry -> !offClass.get(entry))
                .map(ClubLeaderboardEntry::getPlayerKey)
                .collect(Collectors.toSet());

        return FirstRuns.of(entries.stream()
                .filter(entry -> !offClass.get(entry)
                        || (mode == ClassLockMode.WARN && !withHomeRun.contains(entry.getPlayerKey())))
                .toList());
    }
}
