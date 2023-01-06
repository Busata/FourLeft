package io.busata.fourleft.endpoints.discord.messages.crud;

import io.busata.fourleft.api.Routes;
import io.busata.fourleft.api.messages.MessageEvent;
import io.busata.fourleft.api.messages.MessageOperation;
import io.busata.fourleft.api.messages.QueueNames;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static io.busata.fourleft.api.messages.QueueNames.MESSAGES_QUEUE;

@RestController
@RequiredArgsConstructor
public class MessageEndpoint {
    private final RabbitTemplate rabbitTemplate;

    @PostMapping(Routes.MESSAGE_BY_CHANNEL_ID)
    public void createMessage(@PathVariable Long channelId, @RequestBody String content) {
        rabbitTemplate.convertAndSend(MESSAGES_QUEUE, new MessageEvent(MessageOperation.CREATE, channelId, null, content));
    }

    @DeleteMapping(Routes.MESSAGE_BY_CHANNEL_ID_AND_MESSAGE_ID)
    public void deleteMessage(@PathVariable Long channelId, @PathVariable Long messageId) {
        rabbitTemplate.convertAndSend(MESSAGES_QUEUE, new MessageEvent(MessageOperation.DELETE, channelId, messageId, null));
    }

    @PutMapping(Routes.MESSAGE_BY_CHANNEL_ID_AND_MESSAGE_ID)
    public void updateMessage(@PathVariable Long channelId, @PathVariable Long messageId, @RequestBody String content) {
        rabbitTemplate.convertAndSend(MESSAGES_QUEUE, new MessageEvent(MessageOperation.UPDATE, channelId, messageId, content));
    }

}
