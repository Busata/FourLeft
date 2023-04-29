package io.busata.fourleft.endpoints.discord;

import io.busata.fourleft.api.Routes;
import io.busata.fourleft.api.events.MessageEvent;
import io.busata.fourleft.common.MessageOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MessageEndpoint {
    private final ApplicationEventPublisher eventPublisher;

    @PostMapping(Routes.MESSAGE_BY_CHANNEL_ID)
    public void createMessage(@PathVariable Long channelId, @RequestBody String content) {
        eventPublisher.publishEvent(new MessageEvent(MessageOperation.CREATE, channelId, null, content));
    }

    @DeleteMapping(Routes.MESSAGE_BY_CHANNEL_ID_AND_MESSAGE_ID)
    public void deleteMessage(@PathVariable Long channelId, @PathVariable Long messageId) {
        eventPublisher.publishEvent(new MessageEvent(MessageOperation.DELETE, channelId, messageId, null));
    }

    @PutMapping(Routes.MESSAGE_BY_CHANNEL_ID_AND_MESSAGE_ID)
    public void updateMessage(@PathVariable Long channelId, @PathVariable Long messageId, @RequestBody String content) {
        eventPublisher.publishEvent(new MessageEvent(MessageOperation.UPDATE, channelId, messageId, content));
    }

}
