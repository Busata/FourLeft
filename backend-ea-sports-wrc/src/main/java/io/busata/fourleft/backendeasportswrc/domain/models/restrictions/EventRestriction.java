package io.busata.fourleft.backendeasportswrc.domain.models.restrictions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.busata.fourleft.common.RestrictionDisplayMode;
import io.busata.fourleft.common.RestrictionScoringMode;
import io.busata.fourleft.common.RestrictionType;

import java.util.List;
import java.util.Objects;

/**
 * A restriction rule owned by a channel configuration, persisted as jsonb. Targets exactly one of
 * {@code championshipId} (all its events) or {@code eventId} (that event only; wins over a
 * championship-wide rule). {@code penaltyPoints} is only meaningful for scoring mode PENALTY.
 * Violation evaluation lives in the restriction service, not here; this is pure persisted data.
 * {@code bannedVehicles} is display-only (null on older rules): the rest of the car class's catalog when the
 * rule was saved, so posts can name the few banned cars instead of a long permitted list. Enforcement only
 * ever looks at {@code allowedVehicles}.
 */
public record EventRestriction(
        RestrictionType type,
        String championshipId,
        String eventId,
        RestrictionDisplayMode displayMode,
        RestrictionScoringMode scoringMode,
        Integer penaltyPoints,
        List<String> allowedVehicles,
        List<String> bannedVehicles) {

    /** Derived; {@code @JsonIgnore} so it is not persisted as a phantom "eventSpecific" field in the jsonb. */
    @JsonIgnore
    public boolean isEventSpecific() {
        return eventId != null;
    }

    /** True when naming the banned cars is shorter than naming the permitted ones. */
    @JsonIgnore
    public boolean isShownAsBanned() {
        int allowed = allowedVehicles == null ? 0 : allowedVehicles.size();
        return bannedVehicles != null && !bannedVehicles.isEmpty() && bannedVehicles.size() < allowed;
    }

    public boolean appliesTo(String championshipId, String eventId) {
        if (isEventSpecific()) {
            return Objects.equals(this.eventId, eventId);
        }
        return this.championshipId != null && Objects.equals(this.championshipId, championshipId);
    }
}
