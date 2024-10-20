package io.busata.fourleft.backendwrc.infrastructure.schedules;

import io.busata.fourleft.backendwrc.application.CalendarService;
import io.busata.fourleft.backendwrc.application.FIATickerImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class FIATickerUpdateSchedule {

    private final FIATickerImportService tickerImportService;
    private final CalendarService calendarService;

    @Scheduled(initialDelay = 30, fixedDelay=Long.MAX_VALUE, timeUnit = TimeUnit.SECONDS)
    public void initialUpdate() {
        tickerImportService.importTickerEntries(false);
    }
    
    @Scheduled(initialDelay = 60, fixedDelay=15, timeUnit = TimeUnit.SECONDS)
    public void updateTickers() {
        tickerImportService.importTickerEntries(true);
    }

    @Scheduled(initialDelay = 86400, fixedDelay=86400, timeUnit = TimeUnit.SECONDS)
    public void updateCalendar() {
        calendarService.updateCalendar();
    }
}
