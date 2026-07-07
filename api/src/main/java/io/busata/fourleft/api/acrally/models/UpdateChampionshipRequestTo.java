package io.busata.fourleft.api.acrally.models;

import java.time.LocalDateTime;

/** Payload to update a championship's name, start moment and publication status. */
public record UpdateChampionshipRequestTo(
        String name,
        LocalDateTime startsAt,
        String status) {
}
