package io.busata.fourleft.endpoints.views.points.single;

import io.busata.fourleft.api.models.views.PointPairTo;
import io.busata.fourleft.api.models.views.ResultListRestrictionsTo;
import io.busata.fourleft.api.models.views.SinglePointListTo;
import io.busata.fourleft.api.models.views.ViewPointsTo;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.configuration.points.DefaultPointsCalculator;
import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import io.busata.fourleft.endpoints.views.PointsPeriod;
import io.busata.fourleft.endpoints.views.ViewResultToFactory;
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
public class SingleClubViewDefaultPointsFactory {
    private final ClubSyncService clubSyncService;

    public ViewPointsTo createDefaultPoints(PointsPeriod period, SingleClubView resultsView, DefaultPointsCalculator calc) {
        final var club = clubSyncService.getOrCreate(resultsView.getClubId());


        final var resultList = club.getChampionships().stream().filter(championship -> {
                    if (period == PointsPeriod.CURRENT) {
                        return championship.isActive();
                    } else {
                        return championship.isInActive();
                    }
                }).findFirst().stream()
                .flatMap(championship ->  {
                    return championship.getEntries().stream().map(
                            standingEntry -> {
                                return new PointPairTo(standingEntry.getDisplayName(), standingEntry.getNationality(), standingEntry.getTotalPoints().intValue());
                            }
                    );
                }).collect(Collectors.toList());

        return new ViewPointsTo(List.of(new SinglePointListTo("", resultList)));
    }
}
