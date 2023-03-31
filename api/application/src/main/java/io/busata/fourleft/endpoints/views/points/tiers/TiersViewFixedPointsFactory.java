package io.busata.fourleft.endpoints.views.points.tiers;

import io.busata.fourleft.api.models.views.NoResultRestrictionsTo;
import io.busata.fourleft.api.models.views.PointPairTo;
import io.busata.fourleft.api.models.views.ResultListRestrictionsTo;
import io.busata.fourleft.api.models.views.ResultRestrictionsTo;
import io.busata.fourleft.api.models.views.SinglePointListTo;
import io.busata.fourleft.api.models.views.ViewPointsTo;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.configuration.points.FixedPointsCalculator;
import io.busata.fourleft.domain.configuration.results_views.TiersView;
import io.busata.fourleft.domain.tiers.models.Tier;
import io.busata.fourleft.domain.tiers.repository.TierEventRestrictionsRepository;
import io.busata.fourleft.endpoints.views.PointsPeriod;
import io.busata.fourleft.endpoints.views.ViewResultToFactory;
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
    private final ViewResultToFactory viewResultToFactory;
    private final ClubSyncService clubSyncService;
    private final FixedPointChampionshipFetcher fixedPointChampionshipFetcher;
    private final TierEventRestrictionsRepository tierEventRestrictionsRepository;

    public ViewPointsTo createFixedPoints(PointsPeriod period, TiersView resultsView, FixedPointsCalculator calc) {
        return new ViewPointsTo(resultsView.getTiers().stream().map(tier -> {
            return createPointsList(period, resultsView, calc, tier);
        }).collect(Collectors.toList()));
    }

    private SinglePointListTo createPointsList(PointsPeriod period, TiersView resultsView, FixedPointsCalculator calc, Tier tier) {
        final var club = clubSyncService.getOrCreate(tier.getClubId());
        final var resultList = fixedPointChampionshipFetcher.filterChampionships(club.getChampionships(), calc, period)
                .stream()
                .flatMap(championship -> championship.getEvents().stream())
                .filter(Event::isPrevious)
                .map(evt -> {
                    final var restrictions = tierEventRestrictionsRepository.findByTierIdAndChallengeIdAndEventId(tier.getId(), evt.getChallengeId(), evt.getReferenceId());

                    final ResultRestrictionsTo restrictionTo = restrictions.map(viewResultToFactory::create).orElse(new NoResultRestrictionsTo());

                    return viewResultToFactory.createSingleResultTo(tier, resultsView, evt, restrictionTo);
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

        return new SinglePointListTo(tier.getName(),
                entries.entrySet().stream().map(entrySet -> {
                    return new PointPairTo(entrySet.getKey(), nationalities.get(entrySet.getKey()), entrySet.getValue());
                }).sorted(Comparator.comparing(PointPairTo::points).reversed()).toList());
    }


}
