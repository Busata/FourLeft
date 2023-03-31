package io.busata.fourleft.endpoints.views.points.single;

import io.busata.fourleft.api.models.views.ViewPointsTo;
import io.busata.fourleft.domain.configuration.ClubView;
import io.busata.fourleft.domain.configuration.points.DefaultPointsCalculator;
import io.busata.fourleft.domain.configuration.points.FixedPointsCalculator;
import io.busata.fourleft.domain.configuration.points.PointsCalculator;
import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import io.busata.fourleft.endpoints.views.PointsPeriod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SingleClubViewPointsFactory {
    private final SingleClubViewFixedPointsFactory singleClubViewFixedPointsFactory;
    private final SingleClubViewDefaultPointsFactory singleClubViewDefaultPointsFactory;

    public ViewPointsTo create(ClubView view, PointsPeriod period, SingleClubView resultsView) {
        return switch (view.getPointsCalculator()) {
            case FixedPointsCalculator calc ->
                    singleClubViewFixedPointsFactory.createFixedPoints(period, resultsView, calc);
            case DefaultPointsCalculator calc ->
                    singleClubViewDefaultPointsFactory.createDefaultPoints(period, resultsView, calc);
            default -> throw new UnsupportedOperationException("Points calculator not supported");
        };
    }
}
