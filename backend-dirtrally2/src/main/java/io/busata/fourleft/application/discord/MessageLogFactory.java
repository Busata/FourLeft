package io.busata.fourleft.application.discord;

import io.busata.fourleft.api.models.messages.MessageLogTo;
import io.busata.fourleft.domain.discord.bot.models.MessageLog;
import io.busata.fourleft.infrastructure.common.Factory;
import org.springframework.stereotype.Component;

@Factory
public class MessageLogFactory {

    public MessageLog create(MessageLogTo request) {
        MessageLog log = new MessageLog();
        log.setMessageId(request.messageId());
        log.setViewType(request.viewType());
        log.setChannelId(request.channelId());
        log.setMessageType(request.messageType());
        return log;
    }


    public void update(MessageLog log, MessageLogTo request) {
        log.setMessageId(request.messageId());
        log.setViewType(request.viewType());
        log.setChannelId(request.channelId());
        log.setMessageType(request.messageType());
    }

    public MessageLogTo create(MessageLog messageLog) {
        return new MessageLogTo(
                messageLog.getMessageType(),
                messageLog.getViewType(),
                messageLog.getMessageId(),
                messageLog.getChannelId()
        );
    }
}
