package io.busata.fourleft.domain.wrc;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
public class FIATickerEntry {

    @Id
    @GeneratedValue
    UUID id;


    String eventId;

    ZonedDateTime time;

    String referenceId;

    String textHtml;
    String textMarkdown;
    String title;
    String tickerEntryImageUrl;
    String tickerEventKey;

    TickerEntrySource source;

    @Builder
    public FIATickerEntry(String eventId, ZonedDateTime time, String referenceId, String textHtml, String textMarkdown, String title, String tickerEntryImageUrl, String tickerEventKey, TickerEntrySource source) {
        this.eventId = eventId;
        this.time = time;
        this.referenceId = referenceId;
        this.textHtml = textHtml;
        this.textMarkdown = textMarkdown;
        this.title = title;
        this.tickerEntryImageUrl = tickerEntryImageUrl;
        this.tickerEventKey = tickerEventKey;
        this.source = source;
    }
}
