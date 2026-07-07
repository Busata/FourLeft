package io.busata.fourleft.api.acrally.models;

import java.util.List;
import java.util.UUID;

/**
 * Payload to add an event to a championship in one shot: its gap/duration in days and the stages
 * (variants, ordered) and cars it runs — so the owner never has to open the event to finish setting
 * it up. Events aren't named; they're labelled from their stages' locations. {@code variantIds} and
 * {@code carIds} may be empty/null.
 */
public record CreateEventRequestTo(
        Integer gapDays,
        Integer durationDays,
        List<UUID> variantIds,
        List<UUID> carIds) {
}
