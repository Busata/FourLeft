package io.busata.fourleft.backendeasportswrc.application.discord.results;

import java.util.List;
import java.util.Map;

public record ClubStats(
        String vehicleClass,
        String location,
        Long locationID,
        List<String> stages,
        CarStatistics carStatistics
) {
}
