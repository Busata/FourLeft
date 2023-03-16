package io.busata.fourleft.endpoints.views.points.factory.tiers;

import io.busata.fourleft.api.models.views.ViewPointsTo;
import io.busata.fourleft.domain.configuration.points.FixedPointsCalculator;
import io.busata.fourleft.domain.configuration.points.PointsCalculator;
import io.busata.fourleft.domain.configuration.results_views.MergeResultsView;
import io.busata.fourleft.endpoints.views.points.factory.tiers.helpers.MergeResultsViewFixedPointsFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MergeResultsViewPointsFactory {
    private final MergeResultsViewFixedPointsFactory fixedPointsFactory;

    public ViewPointsTo create(PointsCalculator calculator, MergeResultsView resultsView) {
        if(calculator instanceof FixedPointsCalculator calc) {
            return fixedPointsFactory.createFixedPoints(resultsView, calc);
        }

        throw new UnsupportedOperationException("Tiers only support fixed points");
    }
}
