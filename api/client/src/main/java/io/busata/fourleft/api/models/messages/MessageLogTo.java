package io.busata.fourleft.api.models.messages;

import io.busata.fourleft.domain.discord.models.MessageType;

public record MessageLogTo(MessageType messageType, Long messageId, String author, String content, Long channelId) {
}