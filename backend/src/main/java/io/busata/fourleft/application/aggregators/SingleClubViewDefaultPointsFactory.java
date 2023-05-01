package io.busata.fourleft.application.aggregators;

import io.busata.fourleft.api.models.views.PointPairTo;
import io.busata.fourleft.api.models.views.SinglePointListTo;
import io.busata.fourleft.api.models.views.ViewPointsTo;
import io.busata.fourleft.domain.dirtrally2.clubs.StandingEntry;
import io.busata.fourleft.domain.aggregators.results.SingleClubView;
import io.busata.fourleft.application.dirtrally2.ClubService;
import io.busata.fourleft.infrastructure.common.Factory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;

@Factory
@RequiredArgsConstructor
public class SingleClubViewDefaultPointsFactory {
    private final ClubService clubService;

    public ViewPointsTo createDefaultPoints(SingleClubView resultsView) {
        Set<Long> associatedClubs = resultsView.getAssociatedClubs();

        final var club = clubService.getOrCreate(associatedClubs.stream().findFirst().orElseThrow());

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
