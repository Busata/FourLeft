package io.busata.fourleft.api.models.views;

import java.util.List;

public record ViewEventEntryTo(String countryId,
                               String eventName,
                               List<String> stageNames,
                               String stageCondition,
                               String vehicleClass,
                               boolean isCurrent,
                               boolean isFinished) {
}
