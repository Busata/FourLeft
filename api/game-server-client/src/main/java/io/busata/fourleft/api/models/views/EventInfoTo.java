package io.busata.fourleft.api.models.views;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

public record   EventInfoTo(
        String eventId,
        String eventChallengeId,
        String eventName,
        List<String> stageNames,
        String vehicleClass,
        String country,
        LocalDateTime lastUpdate,
        ZonedDateTime endTime
) {
}
