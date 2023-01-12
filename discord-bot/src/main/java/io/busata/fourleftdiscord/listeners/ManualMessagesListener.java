package io.busata.fourleftdiscord.listeners;

import discord4j.common.util.Snowflake;
import io.busata.fourleft.api.messages.MessageEvent;
import io.busata.fourleft.api.messages.MessageOperation;
import io.busata.fourleft.api.messages.QueueNames;
import io.busata.fourleft.domain.discord.models.MessageType;
import io.busata.fourleftdiscord.messages.DiscordMessageGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ManualMessagesListener {
    private final DiscordMessageGateway facade;

    @RabbitListener(queues= QueueNames.MESSAGES_QUEUE)
    public void listen(MessageEvent event) {
        try {
            log.info("Received message event: {}.", event.operation());
            if (event.operation() == MessageOperation.CREATE) {
                facade.postMessage(Snowflake.of(event.channelId()), event.content(), MessageType.RESULTS_POST);
            } else if (event.operation() == MessageOperation.DELETE) {
                facade.removeMessage(Snowflake.of(event.channelId()), Snowflake.of(event.messageId()));
            } else if (event.operation() == MessageOperation.UPDATE) {
                facade.editMessage(Snowflake.of(event.channelId()), Snowflake.of(event.messageId()), event.content());
            }
        }
        catch (Exception ex) {
            log.error("Something went wrong handling the message event", ex);
        }
    }
}
