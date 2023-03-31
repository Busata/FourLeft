package io.busata.wrcserver.schedules;

import io.busata.wrcserver.importer.WRCEventImportService;
import io.busata.wrcserver.importer.WRCTickerImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class WRCServerSchedules {

    private final WRCEventImportService importService;
    private final WRCTickerImportService tickerImportService;

    @Scheduled(fixedRate = 1000 * 60 * 60, initialDelay = 5000)
    public void importRallyEvents() {
        log.info("Importing rally events");
        importService.importEvents();
    }

    @Scheduled(fixedRate = 1000 * 60, initialDelay = 15000)
    public void importTickerEntries() {
        log.info("Importing ticker entries");
        tickerImportService.importNewTickerEntries();
    }
}
