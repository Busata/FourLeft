package io.busata.fourleft.application.aggregators;

import io.busata.fourleft.api.models.views.ViewPointsTo;
import io.busata.fourleft.domain.aggregators.ClubView;
import io.busata.fourleft.domain.aggregators.points.DefaultPointsCalculator;
import io.busata.fourleft.domain.aggregators.points.FixedPointsCalculator;
import io.busata.fourleft.domain.aggregators.results.SingleClubView;
import io.busata.fourleft.infrastructure.common.Factory;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

@Factory
@RequiredArgsConstructor
public class ViewPointsToFactory {
    private final ResultsViewFixedPointsFactory resultsViewFixedPointsFactory;

    private final SingleClubViewDefaultPointsFactory defaultPointsFactory;

    public ViewPointsTo create(ClubView view) {
        if (view.getPointsCalculator() instanceof DefaultPointsCalculator) {
            return createDefaultPoints(view);
        } else if (view.getPointsCalculator() instanceof FixedPointsCalculator fixedPointsCalculator) {
            return createFixedPoints(fixedPointsCalculator, view);
        }
        throw new IllegalStateException("Unexpected value: " + view.getPointsCalculator());
    }

    private ViewPointsTo createDefaultPoints(ClubView view) {
        if (view.getResultsView() instanceof SingleClubView singleClubView) {
            return defaultPointsFactory.createDefaultPoints(singleClubView);
        }

        throw new UnsupportedOperationException("View not supported");
    }
    private ViewPointsTo createFixedPoints(FixedPointsCalculator fixedPointsCalculator, ClubView clubView) {
        return resultsViewFixedPointsFactory.createFixedPoints(clubView, fixedPointsCalculator, clubView.getResultsView());
    }


}
