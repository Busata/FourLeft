package io.busata.fourleft.endpoints.views.points.factory;

import io.busata.fourleft.api.models.views.ViewPointsTo;
import io.busata.fourleft.domain.configuration.ClubView;
import io.busata.fourleft.domain.configuration.results_views.MergeResultsView;
import io.busata.fourleft.domain.configuration.results_views.PartitionView;
import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import io.busata.fourleft.endpoints.views.points.factory.single.SingleClubViewPointsFactory;
import io.busata.fourleft.endpoints.views.points.factory.tiers.MergeResultsViewPointsFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class ViewPointsToFactory {
    private final SingleClubViewPointsFactory singleClubViewPointsFactory;
    private final MergeResultsViewPointsFactory mergeResultsViewPointsFactory;

    public ViewPointsTo create(ClubView view) {
        return switch (view.getResultsView()) {
            case SingleClubView resultsView -> singleClubViewPointsFactory.create(view.getPointsCalculator(), resultsView);
            case MergeResultsView resultsView ->  mergeResultsViewPointsFactory.create(view.getPointsCalculator(), resultsView);
            //case PartitionView resultsView ->  mergeResultsViewPointsFactory.create(view.getPointsCalculator(), resultsView);
            default -> throw new UnsupportedOperationException("View not supported");
        };
    }

}
