package io.busata.fourleft.application.dirtrally2.aggregators;

import java.util.List;

public record FilteredEntryList<T> (List<T> entries, int totalBeforeExclude) {
}
