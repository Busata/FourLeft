package io.busata.fourleft.backendeasportswrc.domain.models.autoposting;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@IdClass(AutoPostEntryId.class)
public class AutopostEntry {

    @Id
    String eventId;

    @Id
    Long messageId;

    @Id
    String playerKey;

    @Id
    Long channelId;

    public AutopostEntry(String eventId, Long channelId, Long messageId, String playerKey) {
        this.eventId = eventId;
        this.channelId = channelId;
        this.messageId = messageId;
        this.playerKey = playerKey;
    }
}
