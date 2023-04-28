package io.busata.fourleft.endpoints.views.points.factory.helpers;

import io.busata.fourleft.api.models.views.PointPairTo;
import io.busata.fourleft.api.models.views.SinglePointListTo;
import io.busata.fourleft.api.models.views.ViewPointsTo;
import io.busata.fourleft.domain.clubs.models.StandingEntry;
import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import io.busata.fourleft.importer.ClubSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SingleClubViewDefaultPointsFactory {
    private final ClubSyncService clubSyncService;

    public ViewPointsTo createDefaultPoints(SingleClubView resultsView) {
        Set<Long> associatedClubs = resultsView.getAssociatedClubs();

        final var club = clubSyncService.getOrCreate(associatedClubs.stream().findFirst().orElseThrow());

        final var resultList = club.findActiveChampionship().or(club::findPreviousChampionship).stream()
                .flatMap(championship -> {
                    return championship.getEntries().stream()
                            .map(this::createPointPair);
                }).toList();

        return new ViewPointsTo(List.of(new SinglePointListTo("", resultList)));
    }
    private PointPairTo createPointPair(StandingEntry standingEntry) {
        return new PointPairTo(standingEntry.getDisplayName(), standingEntry.getNationality(), standingEntry.getTotalPoints().intValue());
    }

}
