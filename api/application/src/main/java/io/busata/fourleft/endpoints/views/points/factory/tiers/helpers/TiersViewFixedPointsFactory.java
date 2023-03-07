package io.busata.fourleft.endpoints.views.points.factory.tiers.helpers;

import io.busata.fourleft.api.models.views.PointPairTo;
import io.busata.fourleft.api.models.views.SinglePointListTo;
import io.busata.fourleft.api.models.views.ViewPointsTo;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.configuration.points.FixedPointsCalculator;
import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import io.busata.fourleft.domain.configuration.results_views.TieredView;
import io.busata.fourleft.endpoints.views.results.factory.ResultListToFactory;
import io.busata.fourleft.importer.ClubSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TiersViewFixedPointsFactory {
    private final ResultListToFactory resultListToFactory;
    private final ClubSyncService clubSyncService;
    private final FixedPointChampionshipFetcher fixedPointChampionshipFetcher;

    public ViewPointsTo createFixedPoints(TieredView resultsView, FixedPointsCalculator calc) {
        return new ViewPointsTo(resultsView.getResultViews().stream().map(tier -> {
            return createPointsList(resultsView, calc, tier);
        }).collect(Collectors.toList()));
    }

    private SinglePointListTo createPointsList(TieredView resultsView, FixedPointsCalculator calc, SingleClubView singleClubView) {
        final var club = clubSyncService.getOrCreate(singleClubView.getClubId());
        final var resultList = fixedPointChampionshipFetcher.filterChampionships(club.getChampionships(), calc)
                .stream()
                .flatMap(championship -> championship.getEvents().stream())
                .filter(Event::isPrevious)
                .map(evt -> {
                    return resultListToFactory.createResultList(singleClubView, evt);
                })
                .toList();

        Map<String, Integer> entries = new HashMap<>();
        Map<String, String> nationalities = new HashMap<>();

        resultList.forEach(list -> {
            list.results().forEach(entry -> {

                // TODO
               /* if(list.restrictions() instanceof ResultListRestrictionsTo to) {
                    if(!to.isValidVehicle(entry.vehicle())) {
                        return;
                    }
                }*/

                entries.putIfAbsent(entry.racenet(), 0);
                nationalities.putIfAbsent(entry.racenet(), entry.nationality());

                if (entry.isDnf()) {
                    return;
                }

                entries.computeIfPresent(entry.racenet(), (key, value) -> {
                    return value + calc.getPointSystem().getPoints(entry.activityRank().intValue()) +
                            calc.getPointSystem().getPowerStagePoints(entry.powerstageRank().intValue());

                });
            });
        });

        return new SinglePointListTo(singleClubView.getName(),
                entries.entrySet().stream().map(entrySet -> {
                    return new PointPairTo(entrySet.getKey(), nationalities.get(entrySet.getKey()), entrySet.getValue());
                }).sorted(Comparator.comparing(PointPairTo::points).reversed()).toList());
    }


}
