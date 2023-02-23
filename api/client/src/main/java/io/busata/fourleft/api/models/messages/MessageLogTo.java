package io.busata.fourleft.api.models.messages;

import io.busata.fourleft.domain.discord.bot.models.MessageType;
import io.busata.fourleft.domain.discord.bot.models.ViewType;

public record MessageLogTo(MessageType messageType, ViewType viewType, Long messageId, Long channelId) {
}