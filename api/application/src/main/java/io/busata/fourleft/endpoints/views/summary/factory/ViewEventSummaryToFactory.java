package io.busata.fourleft.endpoints.views.summary.factory;

import io.busata.fourleft.api.models.views.ViewEventEntryTo;
import io.busata.fourleft.api.models.views.ViewEventSummaryTo;
import io.busata.fourleft.domain.clubs.models.Championship;
import io.busata.fourleft.domain.clubs.models.Club;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.clubs.models.Stage;
import io.busata.fourleft.domain.configuration.ClubView;
import io.busata.fourleft.domain.configuration.results_views.MergeResultsView;
import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import io.busata.fourleft.importer.ClubSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ViewEventSummaryToFactory {
    private final ClubSyncService clubSyncService;

    public ViewEventSummaryTo create(ClubView view) {
        return switch (view.getResultsView()) {
            case SingleClubView resultsView -> createSingleClub(resultsView.getClubId());
            case MergeResultsView resultsView -> createMergedView(resultsView.getResultViews());
            default -> throw new UnsupportedOperationException("View not supported");
        };
    }

    private ViewEventSummaryTo createMergedView(List<SingleClubView> tiers) {
        return createSingleClub(tiers.get(0).getClubId()); //TODO
    }

    private ViewEventSummaryTo createSingleClub(long clubId) {
        Club club = clubSyncService.getOrCreate(clubId);

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
                    event.getStages().stream().map(Stage::getName).collect(Collectors.toList()),
                    firstStageCondition,
                    vehicleClass,
                    isCurrent,
                    isFinished);
        }).collect(Collectors.toList());
    }
}
