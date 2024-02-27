package io.busata.fourleft.backendeasportswrc.application.discord.results;

import java.util.Map;

public record CarStatistics(Map<String, Double> carPercentages,
                            Map<String, Long> carEntries,
                            Map<String, Long> carTopEntries) {
}
