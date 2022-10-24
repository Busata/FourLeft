package io.busata.fourleft.endpoints.discord.messages.crud;

import io.busata.fourleft.api.Routes;
import io.busata.fourleft.api.models.messages.MessageCreateEvent;
import io.busata.fourleft.api.models.messages.MessageDeleteEvent;
import io.busata.fourleft.api.models.messages.MessageEvent;
import io.busata.fourleft.api.models.messages.MessageUpdateEvent;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
public class MessageEndpoint {

    List<MessageEvent> events = new ArrayList<>();

    @GetMapping(Routes.MESSAGE_EVENTS)
    public List<MessageEvent> getEvents() {
        return events;
    }

    @PostMapping(Routes.MESSAGE_EVENT_BY_EVENT_ID)
    public void completedEvent(@PathVariable UUID eventId) {
        this.events.removeIf(messageEvent -> messageEvent.getId().equals(eventId));
    }

    @PostMapping(Routes.MESSAGE_BY_CHANNEL_ID)
    public void createMessage(@PathVariable Long channelId, @RequestBody String content) {
        events.add(new MessageCreateEvent(UUID.randomUUID(), channelId, content));
    }

    @DeleteMapping(Routes.MESSAGE_BY_CHANNEL_ID_AND_MESSAGE_ID)
    public void deleteMessage(@PathVariable Long channelId, @PathVariable Long messageId) {
        events.add(new MessageDeleteEvent(UUID.randomUUID(), channelId, messageId));

    }

    @PutMapping(Routes.MESSAGE_BY_CHANNEL_ID_AND_MESSAGE_ID)
    public void updateMessage(@PathVariable Long channelId, @PathVariable Long messageId, @RequestBody String content) {
        events.add(new MessageUpdateEvent(UUID.randomUUID(), channelId, messageId, content));
    }



}
