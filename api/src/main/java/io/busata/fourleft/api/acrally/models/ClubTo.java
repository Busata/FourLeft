package io.busata.fourleft.api.acrally.models;

import java.time.LocalDateTime;
import java.util.UUID;

/** A community club as shown on the ACRally dashboard. */
public record ClubTo(
        UUID id,
        String name,
        String description,
        String socialLink,
        String createdByDisplayName,
        long memberCount,
        boolean member,
        boolean owner,
        LocalDateTime createdAt) {
}
