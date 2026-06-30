package io.busata.fourleftdiscord.eawrcsports.commands;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import io.busata.fourleft.api.easportswrc.models.DiscordClubCreateConfigurationTo;
import io.busata.fourleft.api.easportswrc.models.DiscordClubRemoveConfigurationTo;
import io.busata.fourleftdiscord.eawrcsports.EAWRCBackendApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigureCommandHandler extends ListenerAdapter {
    private final JDA client;
    private final EAWRCBackendApi api;

    @PostConstruct
    public void setupListener() {
        client.addEventListener(this);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("fourleft")) {
            return;
        }
        if (!"configure".equals(event.getSubcommandGroup())) {
            return;
        }

        long guildId = event.getGuild() != null ? event.getGuild().getIdLong() : -1L;
        long channelId = event.getChannelIdLong();

        if ("track".equals(event.getSubcommandName())) {
            String clubId = event.getOption("clubid", OptionMapping::getAsString);
            boolean autoposts = event.getOption("autoposts") != null ? event.getOption("autoposts").getAsBoolean() : true;

            api.createChannelConfiguration(new DiscordClubCreateConfigurationTo(guildId, channelId, clubId, autoposts));

            event.reply("Club %s will be tracked for this channel".formatted(clubId)).setEphemeral(true).queue();
        } else if ("untrack".equals(event.getSubcommandName())) {
            String clubId = event.getOption("clubid", OptionMapping::getAsString);

            api.removeChannelConfiguration(new DiscordClubRemoveConfigurationTo(guildId, channelId, clubId));

            event.reply("Club %s no longer tracked for this channel".formatted(clubId)).setEphemeral(true).queue();
        }
    }
}
