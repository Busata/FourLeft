package io.busata.fourleft.api.acrally.models;

import java.time.LocalDate;

/** Payload to schedule a new championship in a club. */
public record CreateChampionshipRequestTo(
        String name,
        LocalDate startDate) {
}
