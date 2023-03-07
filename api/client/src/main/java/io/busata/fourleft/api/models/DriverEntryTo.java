package io.busata.fourleft.api.models;


import java.util.List;

public record DriverEntryTo (
        String racenet,
        String nationality,
        PlatformTo platform,

        Long activityRank,
        String activityTotalTime,
        String activityTotalDiff,

        Long powerstageRank,
        String powerstageTotalTime,
        String powerstageTotalDiff,

        Boolean isDnf,

        List<String> vehicles
) {}
