package io.busata.fourleft.backendeasportswrc.domain.models.autoposting;


import java.io.Serializable;

public class AutoPostEntryId implements Serializable {
    private String eventId;
    private Long messageId;
    private Long channelId;
    private String playerKey;
}
