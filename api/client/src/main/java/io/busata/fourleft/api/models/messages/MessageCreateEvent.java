package io.busata.fourleft.api.models.messages;

import java.util.UUID;

public record MessageCreateEvent(java.util.UUID uuid, Long channelId, String content) implements MessageEvent {
    @Override
    public UUID getId() {
        return uuid;
    }
}
