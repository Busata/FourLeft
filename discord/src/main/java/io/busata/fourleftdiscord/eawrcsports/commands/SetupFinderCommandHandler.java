package io.busata.fourleftdiscord.eawrcsports.commands;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono;
import io.busata.fourleft.api.easportswrc.models.SetupChannelResultTo;
import io.busata.fourleftdiscord.eawrcsports.EAWRCBackendApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SetupFinderCommandHandler {
    private final GatewayDiscordClient client;
    private final EAWRCBackendApi api;

    @PostConstruct
    public void setupListener() {
        client.on(ChatInputInteractionEvent.class, event -> {
            if (event.getCommandName().equals("wrc")) {

                return event.getOption("setup").map(action -> {

                    String country = action.getOption("country")
                            .flatMap(ApplicationCommandInteractionOption::getValue)
                            .map(ApplicationCommandInteractionOptionValue::asString).orElseThrow();

                    String car = action.getOption("car")
                            .flatMap(ApplicationCommandInteractionOption::getValue)
                            .map(ApplicationCommandInteractionOptionValue::asString).orElseThrow();

                    return event.deferReply().then(findAndReplySetups(event, country, car));
                }).orElse(Mono.empty());

            }
            return Mono.empty();
        }).subscribe();
    }

    private Mono<InteractionApplicationCommandCallbackReplyMono> findAndReplySetups(ChatInputInteractionEvent event, String country, String car) {
        return findChannels(country, car).map(channels -> {
            if (channels.isEmpty()) {
                return event.reply("Could not find any setups.").withEphemeral(true);
            }
            String channelResult = channels.stream().map(result -> {
                return "<#%s>".formatted(result.id());
            }).collect(Collectors.joining("n"));
            return event.reply("Found the following setups:\n%s".formatted(channelResult)).withEphemeral(true);
        });
    }

    private Mono<List<SetupChannelResultTo>> findChannels(String country, String car) {
        List<SetupChannelResultTo> setupChannelResultToStream = api.getChannels().stream().filter(channel -> channel.name().toLowerCase().contains(country.toLowerCase()) && channel.name().toLowerCase().contains(car.toLowerCase())).toList();
        return Mono.just(setupChannelResultToStream);
    }

}
