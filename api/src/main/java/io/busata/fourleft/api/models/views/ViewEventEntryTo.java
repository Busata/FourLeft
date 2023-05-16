package io.busata.fourleft.api.models.views;

import java.time.ZonedDateTime;
import java.util.List;

public record ViewEventEntryTo(String countryId,
                               String eventName,
                               ZonedDateTime startTime,
                               ZonedDateTime endTime,
                               List<String> stageNames,
                               String stageCondition,
                               String vehicleClass,
                               boolean isCurrent,
                               boolean isFinished) {
}
