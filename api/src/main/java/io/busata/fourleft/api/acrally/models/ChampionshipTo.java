package io.busata.fourleft.api.acrally.models;

import java.time.LocalDateTime;
import java.util.UUID;

/** A championship as shown in a club's championship list. */
public record ChampionshipTo(
        UUID id,
        UUID clubId,
        String name,
        LocalDateTime startsAt,
        String status,
        int eventCount,
        boolean owner,
        LocalDateTime createdAt) {
}
