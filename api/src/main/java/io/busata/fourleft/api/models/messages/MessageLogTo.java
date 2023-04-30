package io.busata.fourleft.api.models.messages;


import io.busata.fourleft.common.MessageType;
import io.busata.fourleft.common.ViewType;

public record MessageLogTo(MessageType messageType, ViewType viewType, Long messageId, Long channelId) {
}