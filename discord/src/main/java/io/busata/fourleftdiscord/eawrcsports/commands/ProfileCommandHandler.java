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

import java.util.Set;

/** /wrc profile (and its old name /wrc track): claim a racenet account and get a private link to edit it. */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileCommandHandler extends ListenerAdapter {
    private static final Set<String> SUBCOMMANDS = Set.of("profile", "track");

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
        if (!SUBCOMMANDS.contains(event.getSubcommandName())) {
            return;
        }

        String username = event.getOption("racenet", OptionMapping::getAsString);

        ProfileUpdateRequestResultTo response = api.requestTrackingUpdate(new ProfileUpdateRequestTo(username, event.getUser().getId(), event.getUser().getName()));

        event.reply(reply(username, response)).setEphemeral(true).queue();
    }

    private String reply(String username, ProfileUpdateRequestResultTo response) {
        String link = "https://fourleft.io/easportswrc/profile/" + response.requestId();
        return switch (response.outcome()) {
            case LINKED -> "Update your nickname, controller and platform choice [here](" + link + ").";
            case PICK -> username == null || username.isBlank()
                    ? "Pick your Racenet account [here](" + link + ")."
                    : "Couldn't find **" + username + "** (names are case sensitive, and you need to have driven a club event we track). Search for your Racenet account [here](" + link + ").";
            case CONFIRM_DISPUTE -> "**" + response.racenet() + "** is already claimed by someone else. If it's yours, you can dispute the claim [here](" + link + "?claim=" + response.playerId() + ").";
            case DISPUTED -> "You're already disputing **" + response.racenet() + "**. Stuck? Contact @busata";
            case ALREADY_DISPUTED -> "**" + response.racenet() + "** is claimed by someone else and already disputed. Stuck? Contact @busata";
            case DISPUTE_LIMIT -> "**" + response.racenet() + "** is claimed by someone else, and you already have an open dispute on another account. Stuck? Contact @busata";
            case VERIFIED_BY_OTHER -> "**" + response.racenet() + "** has been verified by someone else. If that's wrong, contact @busata";
        };
    }
}
