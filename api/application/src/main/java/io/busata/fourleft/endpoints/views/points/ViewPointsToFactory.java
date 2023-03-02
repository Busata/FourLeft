package io.busata.fourleft.endpoints.views.points;

import io.busata.fourleft.api.models.views.ViewPointsTo;
import io.busata.fourleft.domain.configuration.ClubView;
import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import io.busata.fourleft.domain.configuration.results_views.TieredView;
import io.busata.fourleft.endpoints.views.PointsPeriod;
import io.busata.fourleft.endpoints.views.points.single.SingleClubViewPointsFactory;
import io.busata.fourleft.endpoints.views.points.tiers.TiersViewPointsFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class ViewPointsToFactory {
    private final SingleClubViewPointsFactory singleClubViewPointsFactory;
    private final TiersViewPointsFactory tiersViewPointsFactory;

    public ViewPointsTo create(ClubView view, PointsPeriod period) {
        return switch (view.getResultsView()) {
            case SingleClubView resultsView -> singleClubViewPointsFactory.create(view, period, resultsView);
            case TieredView resultsView ->  tiersViewPointsFactory.create(view, period, resultsView);
            default -> throw new UnsupportedOperationException("View not supported");
        };
    }

}
