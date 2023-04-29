package io.busata.fourleft.infrastructure.clients.wrc;

import java.util.List;

public record WRCTickerSummaryTo(
        int total,
        List<WRCTickerEntryTo> items
) {
}
