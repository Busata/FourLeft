package io.busata.fourleft.endpoints.views.points.factory.tiers;

import io.busata.fourleft.api.models.views.ViewPointsTo;
import io.busata.fourleft.domain.configuration.points.FixedPointsCalculator;
import io.busata.fourleft.domain.configuration.points.PointsCalculator;
import io.busata.fourleft.endpoints.views.points.factory.tiers.helpers.TiersViewFixedPointsFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TieredViewPointsFactory {
    private final TiersViewFixedPointsFactory fixedPointsFactory;

    public ViewPointsTo create(PointsCalculator calculator, TieredView resultsView) {
        if(calculator instanceof FixedPointsCalculator calc) {
            return fixedPointsFactory.createFixedPoints(resultsView, calc);
        }

        throw new UnsupportedOperationException("Tiers only support fixed points");
    }
}
