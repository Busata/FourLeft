package io.busata.fourleft.backendeasportswrc.application.discord.results;

import java.util.List;

public record ClubStats(
        String vehicleClass,
        String location,
        Long locationID,
        List<String> stages,
        CarStatistics carStatistics,
        PlayerStatistics playerStatistics
) {
}
