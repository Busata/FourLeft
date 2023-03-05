package io.busata.fourleft.api.models.views;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
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
}
