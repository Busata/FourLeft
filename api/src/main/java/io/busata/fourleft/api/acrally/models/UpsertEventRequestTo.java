package io.busata.fourleft.api.acrally.models;

/** Payload to update an event's gap/duration in days. */
public record UpsertEventRequestTo(
        Integer gapDays,
        Integer durationDays) {
}
