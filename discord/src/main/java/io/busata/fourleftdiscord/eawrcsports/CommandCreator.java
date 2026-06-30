package io.busata.fourleftdiscord.eawrcsports;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class CommandCreator {
    private final JDA client;

    @PostConstruct
    public void createCommands() {
        SlashCommandData eaWrcSportsCommand = Commands.slash("wrc", "All commands related to EA Sports WRC")
                .addSubcommandGroups(
                        new SubcommandGroupData("results", "Results for the current channels club")
                                .addSubcommands(
                                        new SubcommandData("current", "Current results"),
                                        new SubcommandData("previous", "Previous results"),
                                        new SubcommandData("standings", "Standings")
                                ),
                        new SubcommandGroupData("events", "Event related commands")
                                .addSubcommands(
                                        new SubcommandData("summary", "Summary of the events for the active championship")
                                )
                )
                .addSubcommands(
                        new SubcommandData("track", "Track your name.")
                                .addOption(OptionType.STRING, "racenet", "EA Racenet account name, case sensitive", true),
                        new SubcommandData("setup", "Find the perfect tune in the EA SPORTS WRC setup channel")
                                .addOption(OptionType.STRING, "country", "Country", true)
                                .addOption(OptionType.STRING, "car", "Car name", true)
                );

        SlashCommandData configureBotCommand = Commands.slash("fourleft", "Commands related to the fourleft bot")
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                .addSubcommandGroups(
                        new SubcommandGroupData("configure", "Configuration for the bot")
                                .addSubcommands(
                                        new SubcommandData("track", "Track a club in this channel")
                                                .addOption(OptionType.STRING, "clubid", "The club id (found in the racenet url when navigating to your club)", true)
                                                .addOption(OptionType.BOOLEAN, "autoposts", "If the bot should autopost results for this club (defaults to true)", false),
                                        new SubcommandData("untrack", "Track a club in this channel")
                                                .addOption(OptionType.STRING, "clubid", "The club id (found in the racenet url when navigating to your club)", true)
                                )
                );

        client.updateCommands().addCommands(eaWrcSportsCommand, configureBotCommand).queue();
    }

}
