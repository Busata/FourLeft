package io.busata.fourleft.api.models.configuration;

import io.busata.fourleft.api.models.configuration.results.ResultsViewTo;
import io.busata.fourleft.common.BadgeType;

import java.util.UUID;


public record ClubViewTo(

        UUID id,
        String description,
        BadgeType badgeType,
        ResultsViewTo resultsView,
        PointsCalculatorTo pointsView

) {}
