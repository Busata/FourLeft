package io.busata.fourleft.api.models;

import java.util.List;

public record DriverEntryTo(
        DriverResultTo result,
        DriverRelativeResultTo relative
) {
    public String racenet() {
        return result.racenet();
    }

    public String nationality() {
        return result.nationality();
    }

    public PlatformTo platform() {
        return result.platform();
    }

    public String activityTotalTime() {
        return result.activityTotalTime();
    }

    public String powerStageTotalTime() {
        return result.powerStageTotalTime();
    }

    public Boolean isDnf() {
        return result.isDnf();
    }

    public List<String> vehicles() {
        return result.vehicles();
    }

    public Long activityRank() {
        return relative.activityRank();
    }

    public String activityTotalDiff() {
        return relative.activityTotalDiff();
    }

    public Long powerStageRank() {
        return relative.powerStageRank();
    }

    public String powerStageTotalDiff() {
        return relative.powerStageTotalDiff();
    }
}
