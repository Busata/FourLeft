package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.standings;

import lombok.Builder;

import java.util.List;

@Builder
public record ClubStandingsResultTo(
        String cursorNext,
        String cursorPrevious,
        List<ClubStandingsResultEntryTo> entries
) {
}
