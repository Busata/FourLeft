package io.busata.fourleft.endpoints.views.points.factory.single.helpers;

import io.busata.fourleft.api.models.views.*;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.configuration.points.FixedPointsCalculator;
import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import io.busata.fourleft.endpoints.views.results.factory.ResultListToFactory;
import io.busata.fourleft.endpoints.views.points.factory.tiers.helpers.FixedPointChampionshipFetcher;
import io.busata.fourleft.importer.ClubSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SingleClubViewFixedPointsFactory {
    private final ResultListToFactory resultListToFactory;
    private final ClubSyncService clubSyncService;
    private final FixedPointChampionshipFetcher fixedPointChampionshipFetcher;

    public ViewPointsTo createFixedPoints(SingleClubView resultsView, FixedPointsCalculator calc) {
        final var club = clubSyncService.getOrCreate(resultsView.getClubId());

        final var resultList = fixedPointChampionshipFetcher.filterChampionships(club.getChampionships(), calc)
                .stream()
                .flatMap(championship -> championship.getEvents().stream())
                .filter(Event::isPrevious)
                .map(evt -> {

                    return resultListToFactory.createResultList(resultsView, evt);
                })
                .toList();


        Map<String, Integer> entries = new HashMap<>();
        Map<String, String> nationalities = new HashMap<>();
         resultList.forEach(list -> {
            list.results().forEach(entry -> {

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

        var collect = new SinglePointListTo("",
                entries.entrySet().stream().map(entrySet -> {
                    return new PointPairTo(entrySet.getKey(), nationalities.get(entrySet.getKey()), entrySet.getValue());
                }).sorted(Comparator.comparing(PointPairTo::points).reversed()).toList());

        return new ViewPointsTo(List.of(collect));
    }
}
