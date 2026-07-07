package io.busata.fourleft.api.acrally.models;

import java.time.LocalDateTime;

/** Payload to schedule a new championship in a club. */
public record CreateChampionshipRequestTo(
        String name,
        LocalDateTime startsAt) {
}
