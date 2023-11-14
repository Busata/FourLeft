package io.busata.fourleftdiscord.commands.results;


import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
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
public class ResultsCommandFactory implements CommandProvider {

    private final List<BotCommandOptionHandler> commandHandlers;

    @Override
    public void modify(ImmutableApplicationCommandRequest.Builder rootCommand) {

        var weekly = ApplicationCommandOptionData.builder()
                .name(CommandNames.RESULTS)
                .type(ApplicationCommandOption.Type.SUB_COMMAND_GROUP.getValue())
                .description("Leaderboard results interaction");

        commandHandlers.stream()
                .filter(handler -> handler.getCommand().equalsIgnoreCase(CommandNames.RESULTS))
                .forEach(handler -> {
                    log.info("Adding handler {}", handler);
                    weekly.addOption(handler.buildOption());
                });

        rootCommand.addOption(weekly.build());
    }
}
