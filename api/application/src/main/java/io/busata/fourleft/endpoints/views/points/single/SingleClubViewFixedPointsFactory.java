package io.busata.fourleft.endpoints.views.points.single;

import io.busata.fourleft.api.models.views.PointPairTo;
import io.busata.fourleft.api.models.views.SinglePointListTo;
import io.busata.fourleft.api.models.views.ViewPointsTo;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.configuration.points.DefaultPointsCalculator;
import io.busata.fourleft.domain.configuration.points.FixedPointsCalculator;
import io.busata.fourleft.domain.configuration.points.PointsCalculator;
import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import io.busata.fourleft.endpoints.views.PointsPeriod;
import io.busata.fourleft.endpoints.views.ViewResultToFactory;
import io.busata.fourleft.endpoints.views.points.tiers.FixedPointChampionshipFetcher;
import io.busata.fourleft.importer.ClubSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SingleClubViewFixedPointsFactory {
    private final ViewResultToFactory viewResultToFactory;
    private final ClubSyncService clubSyncService;
    private final FixedPointChampionshipFetcher fixedPointChampionshipFetcher;

    public ViewPointsTo createFixedPoints(PointsPeriod period, SingleClubView resultsView, FixedPointsCalculator calc) {
        final var club = clubSyncService.getOrCreate(resultsView.getClubId());

        final var resultList = fixedPointChampionshipFetcher.filterChampionships(club.getChampionships(), calc, period)
                .stream()
                .flatMap(championship -> championship.getEvents().stream())
                .filter(Event::isPrevious)
                .map(evt -> viewResultToFactory.createSingleResultTo(resultsView, evt))
                .toList();


        Map<String, Integer> entries = new HashMap<>();
        Map<String, String> nationalities = new HashMap<>();
         resultList.forEach(singleResultList -> {
            singleResultList.results().forEach(entry -> {

                entries.putIfAbsent(entry.name(), 0);
                nationalities.putIfAbsent(entry.name(), entry.nationality());

                if (entry.isDnf()) {
                    return;
                }

                entries.computeIfPresent(entry.name(), (key, value) -> {
                    return value + calc.getPointSystem().getPoints(entry.rank().intValue()) +
                            calc.getPointSystem().getPowerStagePoints((int) entry.stageRank());

                });
            });

        });

        var collect = new SinglePointListTo("",
                entries.entrySet().stream().map(entrySet -> {
                    return new PointPairTo(entrySet.getKey(), nationalities.get(entrySet.getKey()), entrySet.getValue());
                }).sorted(Comparator.comparing(PointPairTo::points).reversed()).toList());

        return new ViewPointsTo(List.of(collect));
    }
}
