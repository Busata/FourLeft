package io.busata.fourleft.api.models.overview;

import java.time.ZonedDateTime;

public record CommunityResultSummaryTo(
        String countryName,
        String vehicleClass,
        ZonedDateTime endTime,
        String nationality,
        String vehicle,
        long rank,
        int totalEntries,
        float percentageRank,
        boolean isDnf,
        String totalTime,
        String totalDiff

){
}
