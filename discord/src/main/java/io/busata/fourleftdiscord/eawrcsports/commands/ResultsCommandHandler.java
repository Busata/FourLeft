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
public class ResultsCommandHandler {
    private final GatewayDiscordClient client;
    private final MessageCache messageCache;

    @PostConstruct
    public void setupListener() {
        client.on(ChatInputInteractionEvent.class, event -> {
            if (event.getCommandName().equals("wrc")) {

                return event.getOption("results").flatMap(action -> {
                    return action.getOption("current").map(subAction -> {
                        return getResultsFromCache(event, MessageCacheType.RESULTS_CURRENT);
                    }).or(() -> action.getOption("previous").map(subAction -> {
                        return getResultsFromCache(event, MessageCacheType.RESULTS_PREVIOUS);
                    })).or(() -> action.getOption("standings").map(subAction -> {
                        return getResultsFromCache(event, MessageCacheType.RESULTS_STANDINGS);
                    }));
                }).orElseGet(Mono::empty);

            }
            return Mono.empty();
        }).subscribe();
    }

    private Mono<Void> getResultsFromCache(ChatInputInteractionEvent event, MessageCacheType resultsStandings) {
        return messageCache.getMessage(event.getInteraction().getChannelId().asLong(), resultsStandings).map(results -> {
            return event.reply(InteractionApplicationCommandCallbackSpec.builder().embeds(List.of(results)).build());
        }).orElseGet(() -> event.reply("Could not find any results for this request.").withEphemeral(true));
    }
}
