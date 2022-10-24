package io.busata.fourleft.api.models.configuration;

import java.util.UUID;

public record ClubViewTo(
        UUID id,
        String description,
        ResultsViewTo resultsView,
        PointsCalculatorTo pointsCalculator

) {
}
