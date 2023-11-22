package io.busata.fourleftdiscord.eawrcsports.commands;


import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import io.busata.fourleft.api.easportswrc.models.DiscordClubCreateConfigurationTo;
import io.busata.fourleft.api.easportswrc.models.DiscordClubRemoveConfigurationTo;
import io.busata.fourleft.api.easportswrc.models.ProfileUpdateRequestResultTo;
import io.busata.fourleft.api.easportswrc.models.ProfileUpdateRequestTo;
import io.busata.fourleftdiscord.eawrcsports.EAWRCBackendApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigureCommandHandler {
    private final GatewayDiscordClient client;
    private final EAWRCBackendApi api;

    @PostConstruct
    public void setupListener() {
        client.on(ChatInputInteractionEvent.class, event -> {
            if (event.getCommandName().equals("fourleft")) {

                return event.getOption("configure").flatMap(subOption -> {
                    return subOption.getOption("track").map(action -> {

                        String clubId = action.getOption("clubid")
                                .flatMap(ApplicationCommandInteractionOption::getValue)
                                .map(ApplicationCommandInteractionOptionValue::asString).orElseThrow();

                        boolean autoposts = action.getOption("autoposts")
                                .flatMap(ApplicationCommandInteractionOption::getValue)
                                .map(ApplicationCommandInteractionOptionValue::asBoolean).orElse(true);

                        api.createChannelConfiguration(new DiscordClubCreateConfigurationTo(
                                event.getInteraction().getGuildId().map(Snowflake::asLong).orElse(-1L),
                                event.getInteraction().getChannelId().asLong(),
                                clubId,
                                autoposts
                        ));


                        return event.reply("Club %s will be tracked for this channel".formatted(clubId)).withEphemeral(true).then();
                    });

                }).orElse(Mono.empty());
            }
            return Mono.empty();
        }).subscribe();

        client.on(ChatInputInteractionEvent.class, event -> {
            if (event.getCommandName().equals("fourleft")) {

                return event.getOption("configure").flatMap(subOption -> {
                    return subOption.getOption("untrack").map(action -> {

                        String clubId = action.getOption("clubid")
                                .flatMap(ApplicationCommandInteractionOption::getValue)
                                .map(ApplicationCommandInteractionOptionValue::asString).orElseThrow();

                        api.removeChannelConfiguration(new DiscordClubRemoveConfigurationTo(
                                event.getInteraction().getGuildId().map(Snowflake::asLong).orElse(-1L),
                                event.getInteraction().getChannelId().asLong(),
                                clubId)
                        );


                        return event.reply("Club %s will be tracked for this channel".formatted(clubId)).withEphemeral(true).then();
                    });

                }).orElse(Mono.empty());
            }
            return Mono.empty();
        }).subscribe();
    }
}
