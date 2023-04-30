package io.busata.fourleft.api.events;

import io.busata.fourleft.common.MessageOperation;

public record MessageEvent(MessageOperation operation, Long channelId, Long messageId, String content) {
}
