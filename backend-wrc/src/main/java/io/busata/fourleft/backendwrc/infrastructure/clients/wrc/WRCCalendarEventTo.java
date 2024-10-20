package io.busata.fourleft.backendwrc.infrastructure.clients.wrc;

public record WRCCalendarEventTo(
        String title,
        String location,
        Long startDate,
        Long endDate,
        Integer rallyId,
        Integer eventId
) {
}
