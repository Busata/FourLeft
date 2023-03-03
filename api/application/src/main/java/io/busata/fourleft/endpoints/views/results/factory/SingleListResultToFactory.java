package io.busata.fourleft.endpoints.views.results.factory;

import io.busata.fourleft.api.models.ResultEntryTo;
import io.busata.fourleft.api.models.views.*;
import io.busata.fourleft.domain.clubs.models.BoardEntry;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.clubs.services.BoardEntryFetcher;
import io.busata.fourleft.domain.configuration.results_views.PlayerRestrictions;
import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import io.busata.fourleft.endpoints.club.results.service.ResultEntryToFactory;
import io.busata.fourleft.helpers.Factory;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Factory
@RequiredArgsConstructor
public class SingleListResultToFactory {
    private final EventToFactory eventToFactory;
    private final ResultEntryToFactory resultEntryToFactory;
    private final BoardEntryFetcher boardEntryFetcher;
    private final ResultRestrictionToFactory resultRestrictionToFactory;

    public SingleResultListTo createSingleResultList(SingleClubView view, Event event) {
        List<BoardEntry> entries = boardEntryFetcher.create(view, event);

        if(view.getPlayerRestrictions() == PlayerRestrictions.EXCLUDE) {
            entries = filterEntries(entries, view.getPlayerNames());
        }

        int totalEntries = entries.size();

        List<ResultEntryTo> results = resultEntryToFactory.create(entries);

        if(view.getPlayerRestrictions() == PlayerRestrictions.FILTER) {
            results = filterResults(results, view.getPlayerNames());
        }

        final var restrictions = resultRestrictionToFactory.create(view.getId(), event.getChallengeId(), event.getReferenceId());

        return new SingleResultListTo(
                event.getChampionship().getClub().getName(),
                eventToFactory.create(event),
                restrictions,
                totalEntries,
                results
        );
    }

    private List<BoardEntry> filterEntries(List<BoardEntry> entries, List<String> players) {
        List<String> sanitized = players.stream().map(String::toLowerCase).toList();

        return entries.stream().filter(entry -> sanitized.contains(entry.getName().toLowerCase())).toList();
    }
    private List<ResultEntryTo> filterResults(List<ResultEntryTo> entries, List<String> players) {
        List<String> sanitized = players.stream().map(String::toLowerCase).toList();

        return entries.stream().filter(entry -> sanitized.contains(entry.name().toLowerCase())).toList();
    }

}
