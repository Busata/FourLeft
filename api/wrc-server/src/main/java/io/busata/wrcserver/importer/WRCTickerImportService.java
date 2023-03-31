package io.busata.wrcserver.importer;

import io.busata.wrcserver.domain.WRCEventRepository;
import io.busata.wrcserver.domain.WRCTickerEntry;
import io.busata.wrcserver.domain.WRCTickerEntryRepository;
import io.busata.wrcserver.importer.client.WRCApi;
import io.busata.wrcserver.importer.client.models.WRCTickerEntryImageTo;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WRCTickerImportService {
    private final WRCApi wrcApi;
    private final WRCEventRepository eventRepository;
    private final WRCTickerEntryRepository entryRepository;
    private final RabbitTemplate rabbitTemplate;


    public void importNewTickerEntries() {
        eventRepository.findByActiveIsTrue().ifPresent(event -> {
            final var existingTickerIds = entryRepository.findAllByEventId(event.getWrcReferenceId()).stream().map(WRCTickerEntry::getTickerReferenceId).collect(Collectors.toList());


            List<WRCTickerEntry> tickerEntries = wrcApi.getTickerEntries(event.getWrcReferenceId()).items().stream()
                    .filter(entryTo -> !existingTickerIds.contains(entryTo.id()))
                    .map(entryTo -> new WRCTickerEntry(
                            entryTo.id(),
                            event.getWrcReferenceId(),
                            ZonedDateTime.ofInstant(Instant.ofEpochMilli(entryTo.datetimeUnix()), ZoneId.of("UTC")),
                            entryTo.title(),
                            entryTo.text(),
                            entryTo.tickerEntryImage().map(WRCTickerEntryImageTo::image).orElse(""))
                    ).collect(Collectors.toList());

            entryRepository.saveAll(tickerEntries);

            tickerEntries.forEach(tickerEntry -> {
                rabbitTemplate.convertAndSend("q.wrc.ticker", tickerEntry);
            });

        });
    }

}
