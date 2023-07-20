package io.busata.fourleft.application.wrc;


import io.busata.fourleft.api.events.FIATickerUpdateEvent;
import io.busata.fourleft.api.models.FIATickerUpdateTo;
import io.busata.fourleft.domain.wrc.TickerEntrySource;
import io.busata.fourleft.infrastructure.clients.wrc.WRCApiClient;
import io.busata.fourleft.infrastructure.clients.wrc.TickerEntryImageTo;
import io.busata.fourleft.infrastructure.clients.wrc.TickerSummaryTo;
import io.busata.fourleft.domain.wrc.FIATickerEntry;
import io.busata.fourleft.domain.wrc.FIATickerEntryRepository;
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
public class FIATickerImportService {
    private static final String activeEventId = "2140";
    private static final String activeContentPageId = "176146";

    private final WRCApiClient client;

    private final FIATickerEntryRepository fiaTickerEntryRepository;

    private final ApplicationEventPublisher eventPublisher;


    public void importTickerEntries(boolean triggerEvents) {
        TickerSummaryTo tickerSummary = client.getTickerSummary(activeEventId, activeContentPageId);

        Long currentTickerEntryCount = fiaTickerEntryRepository.countByEventId(activeEventId);

        long tickerEntryDelta = tickerSummary.total() - currentTickerEntryCount;

        if (tickerEntryDelta == 0) {
            log.info("No new ticker entries found");
            return;
        }

        log.info("Found {} new ticker entries", tickerEntryDelta);

        final var existingEntries = fiaTickerEntryRepository.findByEventId(activeEventId);
        final var existingEntryIds = existingEntries.stream().map(FIATickerEntry::getReferenceId).collect(Collectors.toList());

        List<FIATickerEntry> newEntries = tickerSummary.items().stream().filter(tickerEntry -> !existingEntryIds.contains(tickerEntry.id()))
                .map(tickerEntry -> {
                    long dateTimeUnix = Long.parseLong(tickerEntry.datetimeUnix());
                    return FIATickerEntry.builder()
                            .eventId(activeEventId)
                            .referenceId(tickerEntry.id())
                            .time(ZonedDateTime.ofInstant(Instant.ofEpochMilli(dateTimeUnix), ZoneOffset.UTC))
                            .title(tickerEntry.title())
                            .textHtml(tickerEntry.text())
                            .textMarkdown(convertTextToMarkdown(tickerEntry.text()))
                            .tickerEntryImageUrl(tickerEntry.tickerEntryImage().map(TickerEntryImageTo::image).orElse(null))
                            .tickerEventKey(tickerEntry.tickerEvent().typeKey())
                            .source(TickerEntrySource.WRC)
                            .build();
                }).collect(Collectors.toList());

        List<FIATickerUpdateTo> list = newEntries.stream().map(newEntry -> new FIATickerUpdateTo(
                newEntry.getTitle(),
                newEntry.getTickerEventKey(),
                newEntry.getTime().toInstant().atZone(ZoneOffset.UTC).toEpochSecond(),
                newEntry.getTextMarkdown(),
                Optional.ofNullable(newEntry.getTickerEntryImageUrl()).map(url -> {
                    String wrcUrl = "https://www.wrc.com/" + sanitizeUrl(url);
                    return String.format("%s%s", "https://rendercache.busata.io/fit_height/1920?url=", wrcUrl);
                }).orElse(null)
        )).toList();

        if(triggerEvents) {
            log.info("Triggering ticker {} events", list.size());
            eventPublisher.publishEvent(new FIATickerUpdateEvent(list));
        }
        fiaTickerEntryRepository.saveAll(newEntries);
    }

    private static String sanitizeUrl(String url) {
        if (url.startsWith("/")) {
            return url.substring(1);
        } else {
            return url;
        }
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
