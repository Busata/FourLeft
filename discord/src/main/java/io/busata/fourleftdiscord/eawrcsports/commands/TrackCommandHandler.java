package io.busata.fourleftdiscord.eawrcsports.commands;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import io.busata.fourleft.api.easportswrc.models.ProfileUpdateRequestResultTo;
import io.busata.fourleft.api.easportswrc.models.ProfileUpdateRequestTo;
import io.busata.fourleftdiscord.eawrcsports.EAWRCBackendApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackCommandHandler extends ListenerAdapter {
    private final JDA client;
    private final EAWRCBackendApi api;

    @PostConstruct
    public void setupListener() {
        client.addEventListener(this);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("wrc")) {
            return;
        }
        if (event.getSubcommandGroup() != null) {
            return;
        }
        if (!"track".equals(event.getSubcommandName())) {
            return;
        }

        String username = event.getOption("racenet", OptionMapping::getAsString);

        ProfileUpdateRequestResultTo response = api.requestTrackingUpdate(new ProfileUpdateRequestTo(username, event.getUser().getId(), event.getUser().getName()));

        if (response.foundProfile()) {
            event.reply("Update your nickname, controller and platform choice [here](https://fourleft.io/easportswrc/profile/" + response.requestId() + ").").setEphemeral(true).queue();
        } else {
            event.reply("Please ensure you've participated in an event and check for case sensitivity in your Racenet ID before using this command. Stuck? Contact @busata").setEphemeral(true).queue();
        }
    }
}
