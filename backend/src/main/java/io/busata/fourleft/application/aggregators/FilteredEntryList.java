package io.busata.fourleft.application.aggregators;

import java.util.List;

public record FilteredEntryList<T> (List<T> entries, int totalBeforeExclude) {
}
