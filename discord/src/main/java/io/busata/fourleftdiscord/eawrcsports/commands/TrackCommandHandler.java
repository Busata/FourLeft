package io.busata.fourleftdiscord.eawrcsports.commands;


import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
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
public class TrackCommandHandler {
    private final GatewayDiscordClient client;
    private final EAWRCBackendApi api;

    @PostConstruct
    public void setupListener() {
        client.on(ChatInputInteractionEvent.class, event -> {
            if (event.getCommandName().equals("wrc")) {

                return event.getOption("track").map(action -> {

                    String username = action.getOption("racenet")
                            .flatMap(ApplicationCommandInteractionOption::getValue)
                            .map(ApplicationCommandInteractionOptionValue::asString).orElseThrow();

                    ProfileUpdateRequestResultTo response = api.requestTrackingUpdate(new ProfileUpdateRequestTo(username, event.getInteraction().getUser().getId().toString(), event.getInteraction().getUser().getUsername()));

                    if (response.foundProfile()) {
                        return event.reply("Update the alias [here](https://fourleft.io/easportswrc/profile/" + response.requestId() + ").").withEphemeral(true).then();
                    } else {
                        return event.reply("Please ensure you've participated in an event and check for case sensitivity in your Racenet ID before using this command. Stuck? Contact @busata").withEphemeral(true);
                    }


                }).orElse(Mono.empty());

            }
            return Mono.empty();
        }).subscribe();
    }
}
