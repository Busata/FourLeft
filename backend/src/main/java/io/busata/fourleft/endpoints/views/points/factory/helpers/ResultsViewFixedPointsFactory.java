package io.busata.fourleft.endpoints.views.points.factory.helpers;

import io.busata.fourleft.api.models.views.SinglePointListTo;
import io.busata.fourleft.api.models.views.ViewPointsTo;
import io.busata.fourleft.api.models.views.ViewResultTo;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.configuration.ClubView;
import io.busata.fourleft.domain.configuration.points.FixedPointsCalculator;
import io.busata.fourleft.domain.configuration.results_views.ResultsView;
import io.busata.fourleft.endpoints.views.results.factory.ViewResultToFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
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
