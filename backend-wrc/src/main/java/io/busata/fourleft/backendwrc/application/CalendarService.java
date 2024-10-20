package io.busata.fourleft.backendwrc.application;

import io.busata.fourleft.backendwrc.infrastructure.clients.wrc.WRCApiClient;
import io.busata.fourleft.backendwrc.infrastructure.clients.wrc.WRCCalendarEventTo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CalendarService {

    List<WRCCalendarEventTo> events = new ArrayList<>();

    private final WRCApiClient wrcApiClient;


    @EventListener(ApplicationReadyEvent.class)
    public void fetchCalendar() {
        updateCalendar();
    }

    public void updateCalendar() {
        int year = ZonedDateTime.now().getYear();
        this.events = this.wrcApiClient.getCalendar(String.valueOf(year)).content();
    }
    public Optional<Integer> getActiveEventId() {
        var now = ZonedDateTime.now();

        return this.events.stream().filter(event -> {

            Instant start = Instant.ofEpochMilli(event.startDate());
            var zonedStart = ZonedDateTime.ofInstant(start, ZoneOffset.UTC);
            Instant end = Instant.ofEpochMilli(event.endDate());
            var zonedEnd = ZonedDateTime.ofInstant(end, ZoneOffset.UTC);

            return now.isAfter(zonedStart.minusDays(1)) && now.isBefore(zonedEnd.plusDays(1));
        }).map(WRCCalendarEventTo::eventId).findFirst();
    }






}
