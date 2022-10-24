package io.busata.fourleft.endpoints.discord.messages.logs.service;

import io.busata.fourleft.api.models.messages.MessageLogTo;
import io.busata.fourleft.domain.discord.models.MessageLog;
import org.springframework.stereotype.Component;

@Component
public class MessageLogFactory {

    public MessageLog create(MessageLogTo request) {
        MessageLog log = new MessageLog();
        log.setMessageId(request.messageId());
        log.setContent(request.content());
        log.setAuthor(request.author());
        log.setChannelId(request.channelId());
        log.setMessageType(request.messageType());
        return log;
    }

    public MessageLogTo create(MessageLog messageLog) {
        return new MessageLogTo(
                messageLog.getMessageType(),
                messageLog.getMessageId(),
                messageLog.getAuthor(),
                messageLog.getContent(),
                messageLog.getChannelId()
        );
    }
}
