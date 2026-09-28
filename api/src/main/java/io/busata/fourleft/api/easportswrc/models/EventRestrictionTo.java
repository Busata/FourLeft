package io.busata.fourleft.api.easportswrc.models;

import io.busata.fourleft.common.RestrictionDisplayMode;
import io.busata.fourleft.common.RestrictionScoringMode;
import io.busata.fourleft.common.RestrictionType;

import java.util.List;

/**
 * Wire model for an event restriction rule: targets exactly one of {@code championshipId} (all its
 * events) or {@code eventId} (that event only, wins over a championship-wide rule). Used both in the
 * channel configuration CRUD and as the active-rule summary on an event's results.
 * {@code bannedVehicles} is display-only: the complement of the allowlist within the car class's catalog at
 * save time, so results can show whichever list is shorter. Enforcement always uses {@code allowedVehicles}.
 */
public record EventRestrictionTo(
        RestrictionType type,
        String championshipId,
        String eventId,
        RestrictionDisplayMode displayMode,
        RestrictionScoringMode scoringMode,
        Integer penaltyPoints,
        List<String> allowedVehicles,
        List<String> bannedVehicles)
{
}
