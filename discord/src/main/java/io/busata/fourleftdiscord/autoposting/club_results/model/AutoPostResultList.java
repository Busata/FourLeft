package io.busata.fourleftdiscord.autoposting.club_results.model;

import io.busata.fourleft.api.models.DriverEntryTo;
import io.busata.fourleft.api.models.ResultEntryTo;
import io.busata.fourleft.api.models.views.ActivityInfoTo;
import io.busata.fourleft.api.models.views.ResultRestrictionsTo;

import java.util.List;

public record AutoPostResultList(
        String name,
        List<ActivityInfoTo> activityInfoTo,

        List<DriverEntryTo> results
) {
}
