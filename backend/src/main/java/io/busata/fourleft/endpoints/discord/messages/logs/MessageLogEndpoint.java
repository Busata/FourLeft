package io.busata.fourleft.endpoints.discord.messages.logs;


import io.busata.fourleft.api.Routes;
import io.busata.fourleft.api.messages.MessageType;
import io.busata.fourleft.api.models.messages.MessageLogTo;
import io.busata.fourleft.infrastructure.common.TransactionHandler;
import io.busata.fourleft.domain.discord.bot.models.MessageLog;
import io.busata.fourleft.domain.discord.bot.repository.MessageLogRepository;
import io.busata.fourleft.endpoints.discord.messages.logs.service.MessageLogFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class MessageLogEndpoint {
    private final MessageLogRepository messageLogRepository;
    private final MessageLogFactory factory;
    private final TransactionHandler transactionHandler;

    @PostMapping(Routes.DISCORD_ALL_MESSAGES)
    public void postMessage(@RequestBody MessageLogTo request) {
        transactionHandler.runInTransaction(() -> {
            messageLogRepository.findByMessageId(request.messageId()).ifPresentOrElse(messageLog -> {
                factory.update(messageLog, request);
                messageLogRepository.save(messageLog);
            }, () -> {
                messageLogRepository.save(factory.create(request));
            });
        });
    }

    @GetMapping(Routes.DISCORD_MESSAGE_DETAILS)
    public MessageLogTo getMessage(@PathVariable long messageId) {
        return messageLogRepository.findByMessageId(messageId).map(factory::create).orElseThrow();
    }

    @GetMapping(Routes.DISCORD_ALL_MESSAGES)
    public List<MessageLogTo> getMessages() {
        return messageLogRepository.findAll().stream()
                .sorted(Comparator.comparing(MessageLog::getMessageId).reversed())
                .map(factory::create).toList();
    }

    @GetMapping(Routes.DISCORD_MESSAGE)
    public boolean getMessages(@RequestParam long messageId, @RequestParam MessageType messageType) {
        return messageLogRepository.existsByMessageIdAndMessageType(messageId, messageType);
    }
}
