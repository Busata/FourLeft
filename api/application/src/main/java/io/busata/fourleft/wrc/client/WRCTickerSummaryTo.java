package io.busata.fourleft.wrc.client;

import java.util.List;

public record WRCTickerSummaryTo(
        int total,
        List<WRCTickerEntryTo> items
) {
}
