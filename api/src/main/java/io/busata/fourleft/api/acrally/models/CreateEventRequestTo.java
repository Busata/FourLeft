package io.busata.fourleft.api.acrally.models;

import java.util.List;
import java.util.UUID;

/**
 * Payload to add an event to a championship in one shot: its name, its gap/duration in days, and the
 * stages (variants, ordered) and cars it runs — so the owner never has to open the event to finish
 * setting it up. {@code variantIds} and {@code carIds} may be empty/null.
 */
public record CreateEventRequestTo(
        String name,
        Integer gapDays,
        Integer durationDays,
        List<UUID> variantIds,
        List<UUID> carIds) {
}
