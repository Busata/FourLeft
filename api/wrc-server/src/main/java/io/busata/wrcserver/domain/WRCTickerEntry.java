package io.busata.wrcserver.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@Getter
public class WRCTickerEntry {

    @Id
    @GeneratedValue
    UUID id;

    String eventId;
    String tickerReferenceId;
    ZonedDateTime dateTime;

    String title;
    String text;

    String imageUrl;

    public WRCTickerEntry(String tickerReferenceId, String eventId, ZonedDateTime dateTime, String title, String text, String imageUrl) {
        this.tickerReferenceId = tickerReferenceId;
        this.eventId = eventId;
        this.dateTime = dateTime;
        this.title = title;
        this.text = text;
        this.imageUrl = imageUrl;
    }
}
