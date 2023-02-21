package io.busata.fourleft.api.models.messages;

import io.busata.fourleft.domain.discord.models.MessageType;
import io.busata.fourleft.domain.discord.models.ViewType;

public record MessageLogTo(MessageType messageType, ViewType viewType, Long messageId, Long channelId) {
}