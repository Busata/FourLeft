package io.busata.fourleft.api.models.views;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record ActivityInfoTo (
        UUID id,
        String eventId,
        String eventChallengeId,
        String eventName,
        List<String> stageNames,
        String vehicleClass,
        String country,
        LocalDateTime lastUpdate,
        ZonedDateTime endTime,

        ResultRestrictionsTo restrictions
) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityInfoTo that = (ActivityInfoTo) o;
        return eventName.equals(that.eventName) && stageNames.containsAll(that.stageNames) && vehicleClass.equals(that.vehicleClass) && country.equals(that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventName, stageNames, vehicleClass, country);
    }
}
