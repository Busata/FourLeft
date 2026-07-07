package io.busata.fourleft.api.acrally.models;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** A championship with its ordered events, each carrying its variants, cars and derived dates. */
public record ChampionshipDetailTo(
        UUID id,
        UUID clubId,
        String clubName,
        String name,
        LocalDate startDate,
        String status,
        boolean owner,
        List<ChampionshipEventTo> events) {
}
