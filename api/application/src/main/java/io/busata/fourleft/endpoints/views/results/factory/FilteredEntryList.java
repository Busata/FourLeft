package io.busata.fourleft.endpoints.views.results.factory;

import java.util.List;

public record FilteredEntryList<T> (List<T> entries, int totalBeforeExclude) {
}
