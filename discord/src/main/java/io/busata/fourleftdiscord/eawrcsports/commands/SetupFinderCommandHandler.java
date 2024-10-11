package io.busata.fourleftdiscord.eawrcsports.commands;


import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.ChannelData;
import discord4j.discordjson.possible.Possible;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SetupFinderCommandHandler {
    private final GatewayDiscordClient client;

    @PostConstruct
    public void setupListener() {
        client.on(ChatInputInteractionEvent.class, event -> {
            if (event.getCommandName().equals("wrc")) {

                return event.getOption("track").map(action -> {

                    String country = action.getOption("country")
                            .flatMap(ApplicationCommandInteractionOption::getValue)
                            .map(ApplicationCommandInteractionOptionValue::asString).orElseThrow();

                    String car = action.getOption("car")
                            .flatMap(ApplicationCommandInteractionOption::getValue)
                            .map(ApplicationCommandInteractionOptionValue::asString).orElseThrow();


                    return findChannels(country, car).collectList().map(channels-> {
                        if(channels.isEmpty()) {
                            return event.reply("Could not find any setups.").withEphemeral(true);
                        }
                        String channelResult = channels.stream().map(result -> {
                            return "<#%s>".formatted(result.id);
                        }).collect(Collectors.joining("n"));
                        return event.reply("Found the following setups: %s".formatted(channelResult)).withEphemeral(true);
                    });

                }).orElse(Mono.empty());

            }
            return Mono.empty();
        }).subscribe();
    }

    private Flux<ChannelResult> findChannels(String country, String car) {
        try {
            return client.getGuildChannels(Snowflake.of(892050958723469332L))
                    .doOnNext(channel -> {
                        log.info(channel.getId() + "--" + channel.getName());
                    })
                    .filterWhen(topLevelGuildMessageChannel -> {
                        return topLevelGuildMessageChannel
                                .getRestChannel()
                                .getData()
                                .map(ChannelData::parentId)
                                .filter(id -> !id.isAbsent())
                                .map(Possible::get)
                                .filter(Optional::isPresent)
                                .map(Optional::get)
                                .map(Id::asLong)
                                .map(id -> id == 1293964993573683251L);
                    }).filter(channel -> {
                        return channel.getName().toLowerCase().contains(country.toLowerCase()) && channel.getName().toLowerCase().contains(car.toLowerCase());
                    }).map(channel -> {
                        return new ChannelResult(channel.getId().asLong(), channel.getName());
                    });
        } catch (Exception ex) {
            return Flux.empty();
        }
    }

    record ChannelResult(Long id, String channelTitle) {}
}
