package io.busata.fourleft.backendeasportswrc.application.discord.autoposting.projections;

import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;
import io.busata.fourleft.backendeasportswrc.domain.models.Event;

import java.time.Duration;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * The entries of one autopost message. For a MIXED channel {@code mixed} (identity-keyed — a driver can
 * enter two classes) carries each entry's own event, class tag and overall rank/gap, and
 * {@code vehicleClasses} replaces the header's single car class; both are empty/null otherwise.
 */
public record AutoPostMessageSummary(Event event, int totalEntries, List<ClubLeaderboardEntry> entries,
                                     Map<ClubLeaderboardEntry, MixedEntry> mixed, String vehicleClasses) {

    public record MixedEntry(String eventId, String tag, long rank, Duration delta) {
    }

    public AutoPostMessageSummary(Event event, int totalEntries, List<ClubLeaderboardEntry> entries) {
        this(event, totalEntries, entries, new IdentityHashMap<>(), null);
    }

    public boolean isMixed() {
        return !mixed.isEmpty();
    }

    public String eventIdOf(ClubLeaderboardEntry entry) {
        MixedEntry mixedEntry = mixed.get(entry);
        return mixedEntry != null ? mixedEntry.eventId() : event.getId();
    }

    /** The same entries, e.g. a prefix that fits Discord's message limit. */
    public AutoPostMessageSummary withEntries(List<ClubLeaderboardEntry> subset) {
        return new AutoPostMessageSummary(event, totalEntries, subset, mixed, vehicleClasses);
    }
}
