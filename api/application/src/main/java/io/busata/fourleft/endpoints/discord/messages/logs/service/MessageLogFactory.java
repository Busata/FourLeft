package io.busata.fourleft.endpoints.discord.messages.logs.service;

import io.busata.fourleft.api.models.messages.MessageLogTo;
import io.busata.fourleft.domain.discord.models.MessageLog;
import org.springframework.stereotype.Component;

@Component
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
