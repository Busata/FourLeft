package io.busata.wrcserver.endpoints;

import io.busata.wrcserver.importer.WRCEventImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WRCTestEndpoint {

    private final WRCEventImportService wrcEventService;

    @GetMapping("/api/wrc/import_events")
    public void test() {
        wrcEventService.importEvents();
    }
}
