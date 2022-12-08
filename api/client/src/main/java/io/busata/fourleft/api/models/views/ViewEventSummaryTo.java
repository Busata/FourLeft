package io.busata.fourleft.api.models.views;

import java.util.List;

public record ViewEventSummaryTo(
        String header,
        List<ViewEventEntryTo> events
) {
}
