package io.busata.fourleft.application.dirtrally2.aggregators;

import io.busata.fourleft.api.models.views.PointPairTo;
import io.busata.fourleft.api.models.views.ResultListTo;
import io.busata.fourleft.api.models.views.SinglePointListTo;
import io.busata.fourleft.domain.aggregators.points.FixedPointsCalculator;
import io.busata.fourleft.infrastructure.common.Factory;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Factory
public class SinglePointListToFactory {

    public SinglePointListTo calculatePoints(FixedPointsCalculator calc, List<ResultListTo> resultLists) {
        Map<String, Integer> entries = new HashMap<>();
        Map<String, String> nationalities = new HashMap<>();
        resultLists.forEach(resultList -> {
            resultList.results().forEach(entry -> {

                entries.putIfAbsent(entry.racenet(), 0);
                nationalities.putIfAbsent(entry.racenet(), entry.nationality());

                if (entry.isDnf()) {
                    return;
                }

                entries.computeIfPresent(entry.racenet(), (key, value) -> {
                    return value + calc.getPointSystem().getPoints(entry.activityRank().intValue()) +
                            calc.getPointSystem().getPowerStagePoints(entry.powerStageRank().intValue());

                });
            });
        });

        return new SinglePointListTo("",
                entries.entrySet().stream().map(entrySet -> {
                    return new PointPairTo(entrySet.getKey(), nationalities.get(entrySet.getKey()), entrySet.getValue());
                }).sorted(Comparator.comparing(PointPairTo::points).reversed()).toList());
    }


}
