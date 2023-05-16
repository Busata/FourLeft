package io.busata.fourleft.api.models;

public record DriverRelativeResultTo(
        Long activityRank,
        float activityPercentageRank,
        String activityTotalDiff,
        Long powerStageRank,
        String powerStageTotalDiff
){
}
