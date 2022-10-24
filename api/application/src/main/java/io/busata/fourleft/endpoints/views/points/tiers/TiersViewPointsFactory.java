package io.busata.fourleft.endpoints.views.points.tiers;

import io.busata.fourleft.api.models.views.ViewPointsTo;
import io.busata.fourleft.domain.configuration.ClubView;
import io.busata.fourleft.domain.configuration.points.FixedPointsCalculator;
import io.busata.fourleft.domain.configuration.results_views.TiersView;
import io.busata.fourleft.endpoints.views.PointsPeriod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TiersViewPointsFactory {
    private final TiersViewFixedPointsFactory fixedPointsFactory;

    public ViewPointsTo create(ClubView view, PointsPeriod period, TiersView resultsView) {
        if(view.getPointsCalculator() instanceof FixedPointsCalculator calc) {
            return fixedPointsFactory.createFixedPoints(period, resultsView, calc);
        }

        throw new UnsupportedOperationException("Tiers only support fixed points");
    }
}
