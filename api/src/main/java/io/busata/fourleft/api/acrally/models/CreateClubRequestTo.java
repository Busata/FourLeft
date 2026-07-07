package io.busata.fourleft.api.acrally.models;

/** Payload to create a new club. */
public record CreateClubRequestTo(
        String name,
        String description,
        String socialLink) {
}
