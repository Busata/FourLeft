package io.busata.fourleft.api.models.views;

import io.busata.fourleft.api.models.ResultEntryTo;

import java.util.List;

public record SingleResultListTo (
        String name,
        EventInfoTo eventInfoTo,

        ResultRestrictionsTo restrictions,
        int totalEntries,
        List<ResultEntryTo> results
) {
}
