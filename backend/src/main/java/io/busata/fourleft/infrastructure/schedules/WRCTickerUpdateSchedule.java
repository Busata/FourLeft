package io.busata.fourleft.infrastructure.schedules;

import io.busata.fourleft.application.wrc.WRCTickerImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

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