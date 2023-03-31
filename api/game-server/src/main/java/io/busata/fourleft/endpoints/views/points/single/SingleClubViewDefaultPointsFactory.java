package io.busata.fourleft.endpoints.views.points.single;

import io.busata.fourleft.api.models.views.PointPairTo;
import io.busata.fourleft.api.models.views.ResultListRestrictionsTo;
import io.busata.fourleft.api.models.views.SinglePointListTo;
import io.busata.fourleft.api.models.views.ViewPointsTo;
import io.busata.fourleft.domain.clubs.models.Championship;
import io.busata.fourleft.domain.clubs.models.Club;
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
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SingleClubViewDefaultPointsFactory {
    private final ClubSyncService clubSyncService;

    public ViewPointsTo createDefaultPoints(PointsPeriod period, SingleClubView resultsView, DefaultPointsCalculator calc) {
        final var club = clubSyncService.getOrCreate(resultsView.getClubId());

        Stream<Championship> requestChampionship = getChampionship(period, club);

        final var resultList = requestChampionship
                .flatMap(championship ->  {
                    return championship.getEntries().stream().map(
                            standingEntry -> {
                                return new PointPairTo(standingEntry.getDisplayName(), standingEntry.getNationality(), standingEntry.getTotalPoints().intValue());
                            }
                    );
                }).collect(Collectors.toList());

        return new ViewPointsTo(List.of(new SinglePointListTo("", resultList)));
    }

    private static Stream<Championship> getChampionship(PointsPeriod period, Club club) {
        Stream<Championship> requestChampionship;
        if(period == PointsPeriod.CURRENT) {
            requestChampionship = club.findActiveChampionship().or(club::findPreviousChampionship).stream();
        } else {
            requestChampionship = club.findPreviousChampionship().stream();
        }
        return requestChampionship;
    }
}
