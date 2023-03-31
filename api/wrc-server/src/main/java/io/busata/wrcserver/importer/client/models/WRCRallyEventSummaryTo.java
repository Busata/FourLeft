package io.busata.wrcserver.importer.client.models;

import java.util.List;

public record WRCRallyEventSummaryTo(
        int total,
        List<WRCRallyEventEntryTo> items

) {
}
