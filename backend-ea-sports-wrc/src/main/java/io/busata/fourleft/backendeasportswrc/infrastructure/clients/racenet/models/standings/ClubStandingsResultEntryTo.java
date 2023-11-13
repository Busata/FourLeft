package io.busata.fourleft.backendeasportswrc.infrastructure.clients.racenet.models.standings;

import lombok.Builder;

import java.time.Duration;

@Builder
public record ClubStandingsResultEntryTo (
        String ssid,
        String displayName,
        int rank,
        int pointsAccumulated,
        int nationalityID
) {
}
