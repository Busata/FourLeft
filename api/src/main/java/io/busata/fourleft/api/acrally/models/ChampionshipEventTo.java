package io.busata.fourleft.api.acrally.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * An event within a championship. {@code label} is derived from the distinct locations of the
 * event's stages (falling back to a placeholder when it has none). {@code opensAt}/{@code closesAt}
 * are derived from the championship start moment and the running gap/duration of the preceding
 * events, preserving the time-of-day.
 */
public record ChampionshipEventTo(
        UUID id,
        String label,
        int position,
        int gapDays,
        int durationDays,
        LocalDateTime opensAt,
        LocalDateTime closesAt,
        List<EventVariantTo> variants,
        List<CarTo> cars) {
}
