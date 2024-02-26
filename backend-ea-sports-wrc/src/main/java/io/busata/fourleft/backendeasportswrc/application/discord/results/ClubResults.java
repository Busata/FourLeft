package io.busata.fourleft.backendeasportswrc.application.discord.results;

import io.busata.fourleft.backendeasportswrc.domain.models.ClubLeaderboardEntry;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

public record ClubResults(
        String clubId,
        String championshipName,
        String location,
        Long locationID,
        Long lastStageRouteID,
        String vehicleClass,
        Long vehicleClassID,
        String weatherSeason,
        Long weatherSeasonID,
        String lastStageWeatherAndSurface,
        Long lastStageWeatherAndSurfaceId,
        LocalDateTime lastUpdated,
        ZonedDateTime eventCloseDate,
        List<String> stages,
        List<ClubLeaderboardEntry> entries
) {
}
