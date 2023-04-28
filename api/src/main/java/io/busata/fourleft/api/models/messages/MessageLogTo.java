package io.busata.fourleft.api.models.messages;


import io.busata.fourleft.api.messages.MessageType;
import io.busata.fourleft.api.models.ViewType;

public record MessageLogTo(MessageType messageType, ViewType viewType, Long messageId, Long channelId) {
}