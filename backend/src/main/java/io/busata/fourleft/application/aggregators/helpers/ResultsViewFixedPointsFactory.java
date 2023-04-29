package io.busata.fourleft.application.aggregators.helpers;

import io.busata.fourleft.api.models.views.SinglePointListTo;
import io.busata.fourleft.api.models.views.ViewPointsTo;
import io.busata.fourleft.api.models.views.ViewResultTo;
import io.busata.fourleft.domain.dirtrally2.clubs.Event;
import io.busata.fourleft.domain.aggregators.ClubView;
import io.busata.fourleft.domain.aggregators.points.FixedPointsCalculator;
import io.busata.fourleft.domain.aggregators.results.ResultsView;
import io.busata.fourleft.application.aggregators.ViewResultToFactory;
import io.busata.fourleft.infrastructure.common.Factory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Factory
@RequiredArgsConstructor
public class ResultsViewFixedPointsFactory {
    private final FixedPointChampionshipFetcher fixedPointChampionshipFetcher;

    private final SinglePointListToFactory singlePointListToFactory;

    private final ViewResultToFactory viewResultToFactory;

    public ViewPointsTo createFixedPoints(ClubView clubView, FixedPointsCalculator calc, ResultsView resultsView) {
        ViewResultTo viewResult = viewResultToFactory.createViewResultFromResultsView(clubView, resultsView, (club) -> {



            return fixedPointChampionshipFetcher.filterChampionships(club.getChampionships(), calc)
                    .stream()
                    .flatMap(championship -> championship.getEvents().stream())
                    .filter(Event::isPrevious);
        }).orElseThrow();

        SinglePointListTo singlePointList = singlePointListToFactory.calculatePoints(calc, viewResult.getMultiListResults());

        return new ViewPointsTo(List.of(singlePointList));
    }


}
