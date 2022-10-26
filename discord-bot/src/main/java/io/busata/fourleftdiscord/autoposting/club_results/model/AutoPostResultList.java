package io.busata.fourleftdiscord.autoposting.club_results.model;

import io.busata.fourleft.api.models.ResultEntryTo;
import io.busata.fourleft.api.models.views.EventInfoTo;
import io.busata.fourleft.api.models.views.ResultRestrictionsTo;

import java.util.List;

public record AutoPostResultList(
        String name,
        EventInfoTo eventInfoTo,

        ResultRestrictionsTo restrictions,
        List<ResultEntryTo> results
) {
}
