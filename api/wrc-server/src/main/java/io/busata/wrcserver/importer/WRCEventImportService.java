package io.busata.wrcserver.importer;

import io.busata.wrcserver.domain.WRCEvent;
import io.busata.wrcserver.domain.WRCEventRepository;
import io.busata.wrcserver.importer.client.WRCImportService;
import io.busata.wrcserver.importer.client.models.WRCSeasonDataTo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WRCEventImportService {

    private final WRCEventRepository wrcEventRepository;
    private final WRCImportService service;

    @Transactional
    public void importEvents() {
        wrcEventRepository.deleteAll();

        WRCSeasonDataTo wrcSeasonDataTo = service.importEvents();

        List<WRCEvent> collect = wrcSeasonDataTo.rallyEvents().items().stream()
                .map(rallyEventTo -> {
                    return new WRCEvent(rallyEventTo.name(), rallyEventTo.id(), !rallyEventTo.hasEventEnded());
                }).collect(Collectors.toList());

        wrcEventRepository.saveAll(collect);
    }
}
