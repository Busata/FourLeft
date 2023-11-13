package io.busata.fourleftdiscord.eawrcsports;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommandCreator {
    private final GatewayDiscordClient client;

    @PostConstruct
    public void createCommands() {
        long applicationId = client.getRestClient().getApplicationId().block();


        ApplicationCommandRequest eaWrcSportsCommand = ApplicationCommandRequest.builder()
                .name("wrc")
                .description("All commands related to EA Sports WRC")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("results")
                        .description("Results for the current channels club")
                        .type(ApplicationCommandOption.Type.SUB_COMMAND_GROUP.getValue())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name("current")
                                .description("Current results")
                                .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                                .build())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name("previous")
                                .description("Previous results")
                                .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                                .build())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name("standings")
                                .description("Standings")
                                .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                                .build())
                        .build()
                ).addOption(ApplicationCommandOptionData.builder()
                        .name("events")
                        .description("Event related commands")
                        .type(ApplicationCommandOption.Type.SUB_COMMAND_GROUP.getValue())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name("summary")
                                .description("Summary of the events for the active championship")
                                .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                                .build())
                        .build()
                ).addOption(ApplicationCommandOptionData.builder()
                        .name("track")
                        .description("Track your name.")
                        .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name("racenet")
                                .description("Racenet name")
                                .type(ApplicationCommandOption.Type.STRING.getValue())
                                .required(true)
                                .build())
                        .build()
                ).build();


        Map<String, ApplicationCommandData> discordCommands = client.getRestClient()
                .getApplicationService()
                .getGlobalApplicationCommands(applicationId)
                .collectMap(ApplicationCommandData::name).block();

        if (discordCommands.containsKey(eaWrcSportsCommand.name())) {
            ApplicationCommandData discordGreetCmd = discordCommands.get(eaWrcSportsCommand.name());
            long discordGreetCmdId = discordGreetCmd.id().asLong();

            client.getRestClient()
                    .getApplicationService()
                    .modifyGlobalApplicationCommand(applicationId, discordGreetCmdId, eaWrcSportsCommand)
                    .subscribe();

        } else {
            client.getRestClient().getApplicationService()
                    .createGlobalApplicationCommand(applicationId, eaWrcSportsCommand)
                    .subscribe();
        }


    }

}
