package io.busata.fourleft.application.aggregators;

import io.busata.fourleft.api.models.views.ViewEventEntryTo;
import io.busata.fourleft.api.models.views.ViewEventSummaryTo;
import io.busata.fourleft.domain.aggregators.results.PartitionView;
import io.busata.fourleft.domain.aggregators.results.ResultsView;
import io.busata.fourleft.domain.dirtrally2.clubs.Championship;
import io.busata.fourleft.domain.dirtrally2.clubs.Club;
import io.busata.fourleft.domain.dirtrally2.clubs.Event;
import io.busata.fourleft.domain.dirtrally2.clubs.Stage;
import io.busata.fourleft.domain.aggregators.results.MergeResultsView;
import io.busata.fourleft.domain.aggregators.results.SingleClubView;
import io.busata.fourleft.application.dirtrally2.ClubService;
import io.busata.fourleft.infrastructure.common.Factory;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Factory
@RequiredArgsConstructor
public class ViewEventSummaryToFactory {
    private final ClubService clubService;

    public ViewEventSummaryTo create(ResultsView view) {
        if (view instanceof SingleClubView resultsView) {
            return createSingleClub(resultsView.getClubId());
        } else if (view instanceof MergeResultsView resultsView) {
            return createMergedView(resultsView.getResultViews());
        } else if (view instanceof PartitionView resultsView) {
            return create(resultsView.getResultsView());
        }
        throw new UnsupportedOperationException("View not supported");
    }

    private ViewEventSummaryTo createMergedView(List<SingleClubView> tiers) {
        return createSingleClub(tiers.get(0).getClubId());
    }

    private ViewEventSummaryTo createSingleClub(long clubId) {
        Club club = clubService.getOrCreate(clubId);

        return club.findActiveChampionship().map(championship ->
                new ViewEventSummaryTo(
                        championship.getName(),
                        createEventSummary(championship)
                )).orElse(null);
    }

    private static List<ViewEventEntryTo> createEventSummary(Championship championship) {
        return championship.getEvents().stream().sorted(Comparator.comparing(Event::getReferenceId)).map(event -> {
            var country = event.getCountry();
            var firstStageCondition = event.getFirstStageCondition();
            var vehicleClass = event.getVehicleClass();
            var isCurrent = event.isCurrent();
            var isFinished = event.isPrevious();

            return new ViewEventEntryTo(country,
                    event.getName(),
                    event.getStartTime(),
                    event.getEndTime(),
                    event.getStages().stream().map(Stage::getName).collect(Collectors.toList()),
                    firstStageCondition,
                    vehicleClass,
                    isCurrent,
                    isFinished);
        }).collect(Collectors.toList());
    }
}
