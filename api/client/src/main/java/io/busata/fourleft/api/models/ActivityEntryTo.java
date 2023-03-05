package io.busata.fourleft.api.models;

import java.util.UUID;

public record ActivityEntryTo(
        String time,
        String diff,
        String vehicle,
        UUID activityInfoId
) {
}
