package io.busata.fourleft.api.models;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

public record ClubResultTo(
        String eventId,
        String eventChallengeId,
        String eventName,
        String stageName,
        List<String> stageNames,
        String vehicleClass,
        String country,
        LocalDateTime lastUpdate,
        ZonedDateTime endTime,
        List<ResultEntryTo> entries
) {
}
