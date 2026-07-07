package io.busata.fourleft.api.acrally.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * An event within a championship. {@code opensAt}/{@code closesAt} are derived from the championship
 * start moment and the running gap/duration of the preceding events, preserving the time-of-day.
 */
public record ChampionshipEventTo(
        UUID id,
        String name,
        int position,
        int gapDays,
        int durationDays,
        LocalDateTime opensAt,
        LocalDateTime closesAt,
        List<EventVariantTo> variants,
        List<CarTo> cars) {
}
