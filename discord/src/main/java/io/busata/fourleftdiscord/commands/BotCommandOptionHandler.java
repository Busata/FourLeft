package io.busata.fourleftdiscord.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import reactor.core.publisher.Mono;

import java.util.List;

public interface BotCommandOptionHandler {
    ImmutableApplicationCommandOptionData buildOption();

    default boolean canRespond(Snowflake channelId) {
        final var responseChannels = getResponseChannels();
        return responseChannels.size() == 0 || responseChannels.contains(channelId);
    }

    default List<Snowflake> getResponseChannels() {
        return List.of();
    }

    String getCommand();

    String getOption();


    default boolean canHandle(ChatInputInteractionEvent event) {
        return
                event.getCommandName().equalsIgnoreCase("dr2") &&
                event.getOption(getCommand()).isPresent() &&
                        event.getOption(getCommand()).flatMap(e -> e.getOption(getOption())).isPresent();
    }


    Mono<Void> handle(ChatInputInteractionEvent event, MessageChannel channel);
}
