package io.busata.fourleft.endpoints.views.points.tiers.helpers;

import io.busata.fourleft.api.models.views.PointPairTo;
import io.busata.fourleft.api.models.views.ResultListRestrictionsTo;
import io.busata.fourleft.api.models.views.SinglePointListTo;
import io.busata.fourleft.api.models.views.ViewPointsTo;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.configuration.points.FixedPointsCalculator;
import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import io.busata.fourleft.domain.configuration.results_views.TieredView;
import io.busata.fourleft.endpoints.views.results.SingleListResultToFactory;
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
    private final SingleListResultToFactory singleListResultToFactory;
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
                    return singleListResultToFactory.createSingleResultList(singleClubView, evt);
                })
                .toList();

        Map<String, Integer> entries = new HashMap<>();
        Map<String, String> nationalities = new HashMap<>();

        resultList.forEach(singleResultList -> {
            singleResultList.results().forEach(entry -> {
                if(singleResultList.restrictions() instanceof ResultListRestrictionsTo to) {
                    if(!to.isValidVehicle(entry.vehicle())) {
                        return;
                    }
                }

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

        return new SinglePointListTo(singleClubView.getName(),
                entries.entrySet().stream().map(entrySet -> {
                    return new PointPairTo(entrySet.getKey(), nationalities.get(entrySet.getKey()), entrySet.getValue());
                }).sorted(Comparator.comparing(PointPairTo::points).reversed()).toList());
    }


}
