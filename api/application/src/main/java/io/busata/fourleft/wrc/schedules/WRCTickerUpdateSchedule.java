package io.busata.fourleft.wrc.schedules;

import io.busata.fourleft.wrc.importer.WRCTickerImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(prefix = "io.busata.fourleft.scheduling", name="wrc", havingValue="true", matchIfMissing = true)
@RequiredArgsConstructor
public class WRCTickerUpdateSchedule {

    private final WRCTickerImportService tickerImportService;

    //@Scheduled(initialDelay = 30, fixedDelay=Long.MAX_VALUE, timeUnit = TimeUnit.SECONDS)
    public void initialUpdate() {
        tickerImportService.importTickerEntries(false);
    }
    //@Scheduled(initialDelay = 60, fixedDelay=30, timeUnit = TimeUnit.SECONDS)
    public void updateTickers() {
        tickerImportService.importTickerEntries(true);
    }
}