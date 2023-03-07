package io.busata.fourleft.endpoints.views.results.factory;

import io.busata.fourleft.api.models.DriverEntryTo;
import io.busata.fourleft.api.models.views.*;
import io.busata.fourleft.domain.clubs.models.BoardEntry;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.clubs.services.BoardEntryFetcher;
import io.busata.fourleft.domain.configuration.results_views.PlayerRestrictions;
import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import io.busata.fourleft.endpoints.club.results.service.DriverEntryToFactory;
import io.busata.fourleft.helpers.Factory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Factory
@RequiredArgsConstructor
public class ResultListToFactory {
    private final EventToFactory eventToFactory;
    private final DriverEntryToFactory driverEntryToFactory;
    private final BoardEntryFetcher boardEntryFetcher;
    private final ResultRestrictionToFactory resultRestrictionToFactory;

    public ResultListTo createResultList(SingleClubView view, Event event) {
        List<BoardEntry> entries = boardEntryFetcher.create(view, event);

        if(view.getPlayerRestrictions() == PlayerRestrictions.EXCLUDE) {
            entries = filterEntries(entries, view.getPlayerNames());
        }

        int totalEntries = entries.size();

        List<DriverEntryTo> results = driverEntryToFactory.create(entries);

        if(view.getPlayerRestrictions() == PlayerRestrictions.FILTER) {
            results = filterResults(results, view.getPlayerNames());
        }

        final var restrictions = resultRestrictionToFactory.create(view.getId(), event.getChallengeId(), event.getReferenceId());

        return new ResultListTo(
                event.getChampionship().getClub().getName(),
                List.of(eventToFactory.create(event, restrictions)),
                totalEntries,
                results
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

    private List<BoardEntry> filterEntries(List<BoardEntry> entries, List<String> players) {
        List<String> sanitized = players.stream().map(String::toLowerCase).toList();

        return entries.stream().filter(entry -> sanitized.contains(entry.getName().toLowerCase())).toList();
    }
    private List<DriverEntryTo> filterResults(List<DriverEntryTo> entries, List<String> players) {
        List<String> sanitized = players.stream().map(String::toLowerCase).toList();

        return entries.stream().filter(entry -> sanitized.contains(entry.racenet().toLowerCase())).toList();
    }

}
