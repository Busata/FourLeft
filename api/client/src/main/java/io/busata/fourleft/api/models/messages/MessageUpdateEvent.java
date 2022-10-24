package io.busata.fourleft.api.models.messages;

import java.util.UUID;

public record MessageUpdateEvent(java.util.UUID uuid, Long channelId, Long messageId, String content) implements MessageEvent {

    @Override
    public UUID getId() {
        return uuid;
    }
}
