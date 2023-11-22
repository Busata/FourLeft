package io.busata.fourleftdiscord.eawrcsports;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
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


        ApplicationCommandRequest configureBotCommand = ApplicationCommandRequest.builder()
                .name("fourleft")
                .description("Commands related to the fourleft bot")
                .defaultPermission(false)
                .defaultMemberPermissions(PermissionSet.of(Permission.ADMINISTRATOR).toString())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("configure")
                        .description("Configuration for the bot")
                        .type(ApplicationCommandOption.Type.SUB_COMMAND_GROUP.getValue())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name("track")
                                .description("Track a club in this channel")
                                .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                                .addOption(ApplicationCommandOptionData.builder()
                                        .name("clubid")
                                        .description("The club id (found in the racenet url when navigating to your club)")
                                        .required(true)
                                        .type(ApplicationCommandOption.Type.STRING.getValue())
                                        .build())
                                .addOption(ApplicationCommandOptionData.builder()
                                        .name("autoposts")
                                        .description("If the bot should autopost results for this club (defaults to true)")
                                        .required(false)
                                        .type(ApplicationCommandOption.Type.BOOLEAN.getValue())
                                        .build())
                                .build()
                        )
                        .build()
                ).build();

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
                                .description("EA Racenet account name, case sensitive")
                                .type(ApplicationCommandOption.Type.STRING.getValue())
                                .required(true)
                                .build())
                        .build()
                ).build();


        Map<String, ApplicationCommandData> discordCommands = client.getRestClient()
                .getApplicationService()
                .getGlobalApplicationCommands(applicationId)
                .collectMap(ApplicationCommandData::name).block();

        updateOrCreateCommand(applicationId, eaWrcSportsCommand, discordCommands);
        updateOrCreateCommand(applicationId, configureBotCommand, discordCommands);


    }

    private void updateOrCreateCommand(long applicationId, ApplicationCommandRequest botCommand, Map<String, ApplicationCommandData> discordCommands) {
        if (discordCommands.containsKey(botCommand.name())) {
            ApplicationCommandData command = discordCommands.get(botCommand.name());
            long commandId = command.id().asLong();

            client.getRestClient()
                    .getApplicationService()
                    .modifyGlobalApplicationCommand(applicationId, commandId, botCommand)
                    .subscribe();

        } else {
            client.getRestClient().getApplicationService()
                    .createGlobalApplicationCommand(applicationId, botCommand)
                    .subscribe();
        }
    }

}
