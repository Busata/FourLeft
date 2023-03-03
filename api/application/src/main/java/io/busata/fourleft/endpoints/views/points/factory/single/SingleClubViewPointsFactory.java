package io.busata.fourleft.endpoints.views.points.factory.single;

import io.busata.fourleft.api.models.views.ViewPointsTo;
import io.busata.fourleft.domain.configuration.points.DefaultPointsCalculator;
import io.busata.fourleft.domain.configuration.points.FixedPointsCalculator;
import io.busata.fourleft.domain.configuration.points.PointsCalculator;
import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import io.busata.fourleft.endpoints.views.points.factory.single.helpers.SingleClubViewDefaultPointsFactory;
import io.busata.fourleft.endpoints.views.points.factory.single.helpers.SingleClubViewFixedPointsFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SingleClubViewPointsFactory {
    private final SingleClubViewFixedPointsFactory singleClubViewFixedPointsFactory;
    private final SingleClubViewDefaultPointsFactory singleClubViewDefaultPointsFactory;

    public ViewPointsTo create(PointsCalculator calculator, SingleClubView resultsView) {
        return switch (calculator) {
            case FixedPointsCalculator calc -> singleClubViewFixedPointsFactory.createFixedPoints(resultsView, calc);
            case DefaultPointsCalculator $ -> singleClubViewDefaultPointsFactory.createDefaultPoints(resultsView);
            default -> throw new UnsupportedOperationException("Points calculator not supported");
        };
    }
}
