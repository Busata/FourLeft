package io.busata.fourleft.api.acrally.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** A championship with its ordered events, each carrying its variants, cars and derived dates. */
public record ChampionshipDetailTo(
        UUID id,
        UUID clubId,
        String clubName,
        String name,
        LocalDateTime startsAt,
        String status,
        boolean owner,
        List<ChampionshipEventTo> events) {
}
