package io.busata.fourleft.endpoints.views.points.single;

import io.busata.fourleft.api.models.views.*;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.configuration.event_restrictions.models.ViewEventRestrictions;
import io.busata.fourleft.domain.configuration.event_restrictions.repository.ViewEventRestrictionsRepository;
import io.busata.fourleft.domain.configuration.points.FixedPointsCalculator;
import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import io.busata.fourleft.domain.options.models.Vehicle;
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
    private final ViewEventRestrictionsRepository viewEventRestrictionsRepository;

    private final FixedPointChampionshipFetcher fixedPointChampionshipFetcher;

    public ViewPointsTo createFixedPoints(PointsPeriod period, SingleClubView resultsView, FixedPointsCalculator calc) {
        final var club = clubSyncService.getOrCreate(resultsView.getClubId());

        final var resultList = fixedPointChampionshipFetcher.filterChampionships(club.getChampionships(), calc, period)
                .stream()
                .flatMap(championship -> championship.getEvents().stream())
                .filter(Event::isPrevious)
                .map(evt -> {
                    final var restrictions = viewEventRestrictionsRepository.findByResultViewIdAndChallengeIdAndEventId(resultsView.getId(), evt.getChallengeId(), evt.getReferenceId());

                    final ResultRestrictionsTo restrictionTo = restrictions.map(this::create).orElse(new NoResultRestrictionsTo());

                    return viewResultToFactory.createSingleResultTo(resultsView, evt, restrictionTo);
                })
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

    public ResultRestrictionsTo create(ViewEventRestrictions viewEventRestrictions) {
        return new ResultListRestrictionsTo(viewEventRestrictions.getVehicles().stream().map(
                this::createVehicle).collect(Collectors.toList()));

    }
    public VehicleTo createVehicle(Vehicle vehicle) {
        return new VehicleTo(
                vehicle.name(),
                vehicle.getDisplayName()
        );
    }
}
