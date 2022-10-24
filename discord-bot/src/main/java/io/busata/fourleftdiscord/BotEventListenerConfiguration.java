package io.busata.fourleftdiscord;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import io.busata.fourleftdiscord.commands.EventListener;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BotEventListenerConfiguration <T extends Event> {
    private final GatewayDiscordClient client;
    private final List<EventListener<T>> eventListeners;

    @PostConstruct
    public void registerListeners() {
        for(EventListener<T> listener : eventListeners) {
            client.on(listener.getEventType())
                    .flatMap(listener::execute)
                    .onErrorResume(listener::handleError)
                    .subscribe();
        }
    }
}
