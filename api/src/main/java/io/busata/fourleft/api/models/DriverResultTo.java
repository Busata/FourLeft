package io.busata.fourleft.api.models;



import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder(toBuilder = true)
public record DriverResultTo(
        String racenet,
        String nationality,
        PlatformTo platform,
        String activityTotalTime,
        String powerStageTotalTime,
        Boolean isDnf,
        List<VehicleEntryTo> vehicles
) {
}
