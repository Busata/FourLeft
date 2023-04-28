package io.busata.fourleft.api.models;

public record DriverRelativeResultTo(
        Long activityRank,
        String activityTotalDiff,
        Long powerStageRank,
        String powerStageTotalDiff
){
}
