package io.busata.fourleft.endpoints.views.results.factory;

import io.busata.fourleft.api.models.views.*;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import io.busata.fourleft.endpoints.club.results.service.DriverEntryToFactory;
import io.busata.fourleft.helpers.Factory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Factory
@RequiredArgsConstructor
public class ResultListToFactory {
    private final DriverEntryToFactory driverEntryToFactory;
    private final EventToFactory eventToFactory;
    private final ResultRestrictionToFactory resultRestrictionToFactory;

    public ResultListTo createResultList(SingleClubView view, Event event) {

        final var filteredEntryList = driverEntryToFactory.create(view, event);

        final var eventRestrictions = resultRestrictionToFactory.create(view.getId(), event.getChallengeId(), event.getReferenceId());

        final var activityInfo = eventToFactory.create(event, eventRestrictions);

        return new ResultListTo(
                event.getChampionship().getClub().getName(),
                List.of(activityInfo),
                filteredEntryList.totalBeforeExclude(),
                filteredEntryList.entries()
        );
    }
}
