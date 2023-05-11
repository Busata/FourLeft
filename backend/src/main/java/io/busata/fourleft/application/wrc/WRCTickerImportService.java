package io.busata.fourleft.application.wrc;


import io.busata.fourleft.api.events.WRCTickerUpdateEvent;
import io.busata.fourleft.api.models.WRCTickerUpdateTo;
import io.busata.fourleft.infrastructure.clients.wrc.WRCApiClient;
import io.busata.fourleft.infrastructure.clients.wrc.WRCTickerEntryImageTo;
import io.busata.fourleft.infrastructure.clients.wrc.WRCTickerSummaryTo;
import io.busata.fourleft.domain.wrc.WRCTickerEntry;
import io.busata.fourleft.domain.wrc.WRCTickerEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WRCTickerImportService {
    private static final String activeEventId = "2122";
    private final WRCApiClient client;
    private final WRCTickerEntryRepository wrcTickerEntryRepository;

    private final ApplicationEventPublisher eventPublisher;


    public void importTickerEntries(boolean triggerEvents) {
        WRCTickerSummaryTo tickerSummary = client.getTickerSummary(activeEventId);

        Long currentTickerEntryCount = wrcTickerEntryRepository.countByEventId(activeEventId);

        long tickerEntryDelta = tickerSummary.total() - currentTickerEntryCount;

        if (tickerEntryDelta == 0) {
            log.info("No new ticker entries found");
            return;
        }

        log.info("Found {} new ticker entries", tickerEntryDelta);

        final var existingEntries = wrcTickerEntryRepository.findByEventId(activeEventId);
        final var existingEntryIds = existingEntries.stream().map(WRCTickerEntry::getReferenceId).collect(Collectors.toList());

        List<WRCTickerEntry> newEntries = tickerSummary.items().stream().filter(tickerEntry -> !existingEntryIds.contains(tickerEntry.id()))
                .map(tickerEntry -> {
                    long dateTimeUnix = Long.parseLong(tickerEntry.datetimeUnix());
                    return WRCTickerEntry.builder()
                            .eventId(activeEventId)
                            .referenceId(tickerEntry.id())
                            .time(ZonedDateTime.ofInstant(Instant.ofEpochMilli(dateTimeUnix), ZoneOffset.UTC))
                            .title(tickerEntry.title())
                            .textHtml(tickerEntry.text())
                            .textMarkdown(convertTextToMarkdown(tickerEntry.text()))
                            .tickerEntryImageUrl(tickerEntry.tickerEntryImage().map(WRCTickerEntryImageTo::image).orElse(null))
                            .tickerEventKey(tickerEntry.tickerEvent().typeKey())
                            .build();
                }).collect(Collectors.toList());

        List<WRCTickerUpdateTo> list = newEntries.stream().map(newEntry -> new WRCTickerUpdateTo(
                newEntry.getTitle(),
                newEntry.getTickerEventKey(),
                newEntry.getTime().toInstant().atZone(ZoneOffset.UTC).toEpochSecond(),
                newEntry.getTextMarkdown(),
                Optional.ofNullable(newEntry.getTickerEntryImageUrl()).map(url -> "https://www.wrc.com/" + url).orElse(null)
        )).toList();

        if(triggerEvents) {
            log.info("Triggering ticker {} events", list.size());
            eventPublisher.publishEvent(new WRCTickerUpdateEvent(list));
        }
        wrcTickerEntryRepository.saveAll(newEntries);
    }

    String convertTextToMarkdown(String textHtml) {
        log.info("Converting {}", textHtml);

        try {
            Document parsed = Jsoup.parse(textHtml);
            log.info("Parsed");

            for (Element span : parsed.getElementsByTag("td")) {
                span.replaceWith(new TextNode(span.text() + "\\u200B\\u200B"));
            }
            for (Element span : parsed.getElementsByTag("th")) {
                span.replaceWith(new TextNode("**" + span.text() + "**\\u200B\\u200B"));
            }
            for (Element span : parsed.getElementsByTag("tr")) {
                span.replaceWith(new TextNode(span.text() + "\\n"));
            }
            for (Element span : parsed.getElementsByTag("<table>")) {
                span.replaceWith(new TextNode(span.text() + "\\n"));
            }

            for (Element span : parsed.getElementsByTag("span")) {
                span.replaceWith(new TextNode(span.text()));
            }

            for (Element li : parsed.getElementsByTag("li")) {
                li.replaceWith(new TextNode("-" + li.text() + "\\n"));
            }

            for (Element ul : parsed.getElementsByTag("ul")) {
                ul.replaceWith(new TextNode(ul.text()));
            }

            for (Element paragraph : parsed.getElementsByTag("p")) {
                paragraph.replaceWith(new TextNode(paragraph.text() + "\\n"));
            }
            log.info("paragraphs replaced");


            String text = parsed.text();
            log.info("Converted {}", text);
            return text;
        } catch (Exception e) {
            log.error("Error converting text to markdown", e);
            return textHtml;
        }
    }

}
