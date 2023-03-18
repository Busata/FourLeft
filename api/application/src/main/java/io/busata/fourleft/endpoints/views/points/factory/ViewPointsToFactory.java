package io.busata.fourleft.endpoints.views.points.factory;

import io.busata.fourleft.api.models.views.ViewPointsTo;
import io.busata.fourleft.domain.configuration.ClubView;
import io.busata.fourleft.domain.configuration.points.DefaultPointsCalculator;
import io.busata.fourleft.domain.configuration.points.FixedPointsCalculator;
import io.busata.fourleft.domain.configuration.results_views.MergeResultsView;
import io.busata.fourleft.domain.configuration.results_views.PartitionView;
import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import io.busata.fourleft.endpoints.views.points.factory.helpers.SingleClubViewDefaultPointsFactory;
import io.busata.fourleft.endpoints.views.points.factory.helpers.ResultsViewFixedPointsFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ViewPointsToFactory {
    private final ResultsViewFixedPointsFactory resultsViewFixedPointsFactory;

    private final SingleClubViewDefaultPointsFactory defaultPointsFactory;

    public ViewPointsTo create(ClubView view) {
        return switch(view.getPointsCalculator()) {
            case DefaultPointsCalculator $ -> createDefaultPoints(view);
            case FixedPointsCalculator fixedPointsCalculator -> createFixedPoints(fixedPointsCalculator, view);
            default -> throw new IllegalStateException("Unexpected value: " + view.getPointsCalculator());
        };
    }

    private ViewPointsTo createDefaultPoints(ClubView view) {
        if (view.getResultsView() instanceof SingleClubView singleClubView) {
            return defaultPointsFactory.createDefaultPoints(singleClubView);
        }

        throw new UnsupportedOperationException("View not supported");
    }
    private ViewPointsTo createFixedPoints(FixedPointsCalculator fixedPointsCalculator, ClubView clubView) {
        return switch(clubView.getResultsView()) {
            case SingleClubView resultsView -> resultsViewFixedPointsFactory.createFixedPoints(clubView, fixedPointsCalculator, resultsView);
            case MergeResultsView mergeResultsView -> resultsViewFixedPointsFactory.createFixedPoints(clubView, fixedPointsCalculator, mergeResultsView);
            case PartitionView partitionView -> resultsViewFixedPointsFactory.createFixedPoints(clubView, fixedPointsCalculator, partitionView);
            default -> throw new IllegalStateException("Unexpected value: " + clubView.getResultsView());
        };
    }


}
