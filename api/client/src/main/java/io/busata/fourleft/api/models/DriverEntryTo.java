package io.busata.fourleft.api.models;

import java.util.List;

public record DriverEntryTo(
        String name,
        String totalTime,
        String totalDiff,
        PlatformTo platform,
        List<ActivityEntryTo> activities
        ) {
}
