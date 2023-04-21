package io.busata.fourleft.schedules;

import io.busata.fourleft.wrc.importer.WRCTickerImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class WRCTickerUpdateSchedule {

    private final WRCTickerImportService tickerImportService;

    @Scheduled(initialDelay = 30, fixedDelay=Long.MAX_VALUE, timeUnit = TimeUnit.SECONDS)
    public void initialUpdate() {
        tickerImportService.importTickerEntries(false);
    }
    @Scheduled(initialDelay = 60, fixedDelay=30, timeUnit = TimeUnit.SECONDS)
    public void updateTickers() {
        tickerImportService.importTickerEntries(true);
    }
}
