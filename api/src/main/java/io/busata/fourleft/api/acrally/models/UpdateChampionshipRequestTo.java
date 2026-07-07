package io.busata.fourleft.api.acrally.models;

import java.time.LocalDate;

/** Payload to update a championship's name, start date and publication status. */
public record UpdateChampionshipRequestTo(
        String name,
        LocalDate startDate,
        String status) {
}
