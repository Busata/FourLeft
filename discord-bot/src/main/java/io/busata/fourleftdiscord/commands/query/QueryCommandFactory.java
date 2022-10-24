package io.busata.fourleftdiscord.commands.query;


import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import io.busata.fourleftdiscord.commands.BotCommandOptionHandler;
import io.busata.fourleftdiscord.commands.CommandNames;
import io.busata.fourleftdiscord.commands.CommandProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Slf4j
@RequiredArgsConstructor
public class QueryCommandFactory implements CommandProvider {

    private final List<BotCommandOptionHandler> commandHandlers;

    @Override
    public ImmutableApplicationCommandRequest create() {
        ImmutableApplicationCommandRequest.Builder commandBuilder = ApplicationCommandRequest.builder()
                .name(CommandNames.QUERY)
                .description("Query users");

        commandHandlers.stream()
                .filter(handler -> handler.getCommand().equalsIgnoreCase(CommandNames.QUERY))
                .forEach(handler -> {
                    log.info("Adding handler {}", handler);
                    commandBuilder.addOption(handler.buildOption());
                });

        return commandBuilder.build();
    }
}
