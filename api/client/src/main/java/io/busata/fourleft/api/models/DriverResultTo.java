package io.busata.fourleft.api.models;


import java.util.List;

public record DriverResultTo(
        String racenet,
        String nationality,
        PlatformTo platform,
        String activityTotalTime,
        String powerStageTotalTime,
        Boolean isDnf,
        List<String> vehicles
) {}
