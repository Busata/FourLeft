package io.busata.fourleft.api.acrally.models;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * An event within a championship. {@code openDate}/{@code closeDate} are derived from the
 * championship start and the running gap/duration of the preceding events.
 */
public record ChampionshipEventTo(
        UUID id,
        String name,
        int position,
        int gapDays,
        int durationDays,
        LocalDate openDate,
        LocalDate closeDate,
        List<EventVariantTo> variants,
        List<CarTo> cars) {
}
