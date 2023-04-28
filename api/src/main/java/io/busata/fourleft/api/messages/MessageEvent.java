package io.busata.fourleft.api.messages;

public record MessageEvent(MessageOperation operation, Long channelId, Long messageId, String content) {
}
