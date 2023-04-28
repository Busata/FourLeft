package io.busata.fourleft.api.models.views;

import io.busata.fourleft.api.models.DriverEntryTo;
import io.busata.fourleft.api.models.DriverResultTo;

import java.util.List;

public record ResultListTo (
        String name,
        List<ActivityInfoTo> activityInfoTo,
        int totalUniqueEntries,
        List<DriverEntryTo> results) {
}