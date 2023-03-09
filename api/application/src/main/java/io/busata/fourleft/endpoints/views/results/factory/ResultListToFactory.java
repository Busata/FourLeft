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
    private final ViewEntryFetcher viewEntryFetcher;
    private final EventToFactory eventToFactory;
    private final DriverEntryToFactory driverEntryToFactory;
    private final ResultRestrictionToFactory resultRestrictionToFactory;

    public ResultListTo createResultList(SingleClubView view, Event event) {

        final var filteredEntryList = viewEntryFetcher.getEntries(view, event);

        final var eventRestrictions = resultRestrictionToFactory.create(view.getId(), event.getChallengeId(), event.getReferenceId());

        final var activityInfo = eventToFactory.create(event, eventRestrictions);

        return new ResultListTo(
                event.getChampionship().getClub().getName(),
                List.of(activityInfo),
                filteredEntryList.totalBeforeExclude(),
                filteredEntryList.entries()
        );
    }

    public List<ResultListTo> mergeResultLists(List<ResultListTo> results) {

        ResultListTo merged = new ResultListTo(
                "",
                results.stream().map(ResultListTo::activityInfoTo).flatMap(List::stream).toList(),
                0,
                driverEntryToFactory.mergeDriverEntries(results.stream().map(ResultListTo::results).toList())
        );

        return List.of(merged);
    }




}
