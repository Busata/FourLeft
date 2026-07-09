package io.busata.fourleft.backendeasportswrc.domain.services.restrictions;

import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;
import io.busata.fourleft.backendeasportswrc.domain.models.DiscordClubConfiguration;
import io.busata.fourleft.backendeasportswrc.domain.models.restrictions.EventRestriction;
import io.busata.fourleft.common.RestrictionType;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Evaluates {@link EventRestriction} rules against leaderboard entries. Pure and stateless so the
 * same logic backs custom scoring, the web overview and Discord result posts. When multiple channel
 * configurations of one club define conflicting rules for the same target, first-found wins.
 */
@Service
public class RestrictionService {

    /**
     * Rule applicable to the given event, event-specific rules winning over championship-wide ones.
     */
    public Optional<EventRestriction> resolveRestriction(List<EventRestriction> rules, String championshipId, String eventId) {
        if (rules == null) {
            return Optional.empty();
        }

        return rules.stream()
                .filter(rule -> rule.appliesTo(championshipId, eventId))
                .max(Comparator.comparing(EventRestriction::isEventSpecific));
    }

    public Optional<EventRestriction> resolveRestriction(DiscordClubConfiguration configuration, String championshipId, String eventId) {
        return resolveRestriction(configuration.getEventRestrictionsOrEmpty(), championshipId, eventId);
    }

    public boolean violates(EventRestriction restriction, ClubLeaderboardEntry entry) {
        if (restriction.type() != RestrictionType.VEHICLE_ALLOWLIST) {
            return false;
        }

        String vehicle = entry.getVehicle();
        if (vehicle == null) {
            return true;
        }

        return restriction.allowedVehicles() == null || restriction.allowedVehicles().stream()
                .noneMatch(allowed -> allowed != null && allowed.trim().equals(vehicle.trim()));
    }

    /**
     * Scoring positions for scoring mode EXCLUDE: compliant entries (DNFs included — they keep
     * occupying a slot, exactly as without a restriction) sorted by accumulated rank get 1..n;
     * violators are absent from the map. Keyed by entry identity (entries have no equals override).
     */
    public Map<ClubLeaderboardEntry, Long> scoringPositions(EventRestriction restriction, Collection<ClubLeaderboardEntry> entries) {
        List<ClubLeaderboardEntry> compliant = entries.stream()
                .filter(entry -> !violates(restriction, entry))
                .sorted(Comparator.comparing(ClubLeaderboardEntry::getRankAccumulated))
                .toList();

        Map<ClubLeaderboardEntry, Long> positions = new HashMap<>();
        for (int i = 0; i < compliant.size(); i++) {
            positions.put(compliant.get(i), (long) i + 1);
        }
        return positions;
    }
}
