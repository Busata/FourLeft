package io.busata.fourleft.endpoints.views.results.factory;

import io.busata.fourleft.api.models.views.*;
import io.busata.fourleft.domain.dirtrally2.clubs.models.Event;
import io.busata.fourleft.domain.views.configuration.results_views.SingleClubView;
import io.busata.fourleft.infrastructure.common.Factory;
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

        final var eventRestrictions = resultRestrictionToFactory.getResultRestrictionsTo(view.getId(), event);

        final var activityInfo = eventToFactory.create(event, eventRestrictions);

        return new ResultListTo(
                view.getName(),
                List.of(activityInfo),
                filteredEntryList.totalBeforeExclude(),
                filteredEntryList.entries()
        );
    }
}
