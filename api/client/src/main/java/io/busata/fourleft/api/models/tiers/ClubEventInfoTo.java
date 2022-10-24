package io.busata.fourleft.api.models.tiers;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

public record ClubEventInfoTo(String eventId,
                              String eventChallengeId,
                              String eventName,
                              String stageName,
                              List<String> stageNames,
                              String vehicleClass,
                              String country,
                              LocalDateTime lastUpdate,
                              ZonedDateTime endTime) {
}
