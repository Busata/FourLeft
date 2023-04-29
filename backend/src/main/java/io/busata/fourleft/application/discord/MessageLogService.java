package io.busata.fourleft.application.discord;

import io.busata.fourleft.common.MessageType;
import io.busata.fourleft.api.models.messages.MessageLogTo;
import io.busata.fourleft.domain.discord.bot.models.MessageLog;
import io.busata.fourleft.domain.discord.bot.repository.MessageLogRepository;
import io.busata.fourleft.infrastructure.common.TransactionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageLogService {

    private final MessageLogRepository messageLogRepository;
    private final MessageLogFactory factory;
    private final TransactionHandler transactionHandler;

    public void postMessage(MessageLogTo request) {
        transactionHandler.runInTransaction(() -> {
            messageLogRepository.findByMessageId(request.messageId()).ifPresentOrElse(messageLog -> {
                factory.update(messageLog, request);
                messageLogRepository.save(messageLog);
            }, () -> {
                messageLogRepository.save(factory.create(request));
            });
        });
    }

    public MessageLogTo getMessageById(long messageId) {
        return messageLogRepository.findByMessageId(messageId).map(factory::create).orElseThrow();
    }

    public List<MessageLogTo> getAllMessages() {

        return messageLogRepository.findAll().stream()
                .sorted(Comparator.comparing(MessageLog::getMessageId).reversed())
                .map(factory::create).toList();
    }

    public boolean getMessageByType(long messageId, MessageType messageType) {
        return messageLogRepository.existsByMessageIdAndMessageType(messageId, messageType);
    }
}
