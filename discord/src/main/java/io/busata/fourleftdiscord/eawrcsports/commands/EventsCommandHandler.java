package io.busata.fourleftdiscord.eawrcsports.commands;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import io.busata.fourleftdiscord.eawrcsports.MessageCache;
import io.busata.fourleftdiscord.eawrcsports.MessageCacheType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventsCommandHandler {
    private final GatewayDiscordClient client;
    private final MessageCache messageCache;

    @PostConstruct
    public void setupListener() {
        client.on(ChatInputInteractionEvent.class, event -> {
            if (event.getCommandName().equals("wrc")) {

                return event.getOption("events").flatMap(action -> {
                    return action.getOption("summary").map(subAction -> {
                        return getResultsFromCache(event, MessageCacheType.EVENTS_SUMMARY);
                    });
                }).orElseGet(Mono::empty);

            }
            return Mono.empty();
        }).subscribe();
    }

    private Mono<Void> getResultsFromCache(ChatInputInteractionEvent event, MessageCacheType messageCacheType) {
        return messageCache.getMessage(event.getInteraction().getChannelId().asLong(), messageCacheType).map(results -> {
            return event.reply(InteractionApplicationCommandCallbackSpec.builder().embeds(List.of(results)).build());
        }).orElseGet(() -> event.reply("No results found"));
    }
}
