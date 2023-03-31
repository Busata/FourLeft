package io.busata.wrcserver.importer.client.models;

public record WRCSeasonDataTo(
        int seasonYear,
        WRCRallyEventSummaryTo rallyEvents
) {
}
