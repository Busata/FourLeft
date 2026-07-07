package io.busata.fourleft.api.acrally.models;

/** Payload to create or update an event's name and its gap/duration in days. */
public record UpsertEventRequestTo(
        String name,
        Integer gapDays,
        Integer durationDays) {
}
