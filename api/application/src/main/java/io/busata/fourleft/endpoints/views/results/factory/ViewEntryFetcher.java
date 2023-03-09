package io.busata.fourleft.endpoints.views.results.factory;

import io.busata.fourleft.api.models.DriverEntryTo;
import io.busata.fourleft.domain.clubs.models.BoardEntry;
import io.busata.fourleft.domain.clubs.models.Event;
import io.busata.fourleft.domain.clubs.services.BoardEntryFetcher;
import io.busata.fourleft.domain.configuration.player_restrictions.PlayerRestrictionFilterType;
import io.busata.fourleft.domain.configuration.results_views.SingleClubView;
import io.busata.fourleft.endpoints.club.results.service.DriverEntryToFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ViewEntryFetcher {
    private final BoardEntryFetcher boardEntryFetcher;
    private final DriverEntryToFactory driverEntryToFactory;

    public FilteredEntryList<DriverEntryTo> getEntries(SingleClubView view, Event event) {
        List<BoardEntry> entries = boardEntryFetcher.create(view, event);

        if(view.getPlayerRestrictions().getFilterType() == PlayerRestrictionFilterType.INCLUDE) {
            entries = includeNames(entries, view.getPlayerRestrictions().getPlayerNames());
        }

        int totalEntries = entries.size();

        List<DriverEntryTo> results = driverEntryToFactory.create(entries);

        if(view.getPlayerRestrictions().getFilterType() == PlayerRestrictionFilterType.FILTER) {
            results = filterNames(results, view.getPlayerRestrictions().getPlayerNames());
        }

        return new FilteredEntryList<>(results, totalEntries);

    }
    private List<BoardEntry> includeNames(List<BoardEntry> entries, List<String> players) {
        List<String> sanitized = players.stream().map(String::toLowerCase).toList();

        return entries.stream().filter(entry -> sanitized.contains(entry.getName().toLowerCase())).toList();
    }
    private List<DriverEntryTo> filterNames(List<DriverEntryTo> entries, List<String> players) {
        List<String> sanitized = players.stream().map(String::toLowerCase).toList();

        return entries.stream().filter(entry -> sanitized.contains(entry.racenet().toLowerCase())).toList();
    }

}
