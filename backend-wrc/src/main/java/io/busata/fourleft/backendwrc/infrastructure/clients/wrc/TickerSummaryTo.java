package io.busata.fourleft.backendwrc.infrastructure.clients.wrc;

import java.util.List;

public record TickerSummaryTo(
        int total,
        List<TickerEntryTo> items
) {
}
