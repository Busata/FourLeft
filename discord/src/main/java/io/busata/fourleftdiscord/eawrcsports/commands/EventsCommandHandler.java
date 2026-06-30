package io.busata.fourleftdiscord.eawrcsports.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import io.busata.fourleftdiscord.eawrcsports.MessageCache;
import io.busata.fourleftdiscord.eawrcsports.MessageCacheType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventsCommandHandler extends ListenerAdapter {
    private final JDA client;
    private final MessageCache messageCache;

    @PostConstruct
    public void setupListener() {
        client.addEventListener(this);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("wrc")) {
            return;
        }
        if (!"events".equals(event.getSubcommandGroup())) {
            return;
        }
        if (!"summary".equals(event.getSubcommandName())) {
            return;
        }

        messageCache.getMessage(event.getChannelIdLong(), MessageCacheType.EVENTS_SUMMARY).ifPresentOrElse(
                embed -> event.replyEmbeds(embed).queue(),
                () -> event.reply("No results found").queue()
        );
    }
}
