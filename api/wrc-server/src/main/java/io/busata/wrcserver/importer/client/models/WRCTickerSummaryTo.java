package io.busata.wrcserver.importer.client.models;

import java.util.List;

public record WRCTickerSummaryTo(
        int total,
        List<WRCTickerEntryTo> items
) {
}
