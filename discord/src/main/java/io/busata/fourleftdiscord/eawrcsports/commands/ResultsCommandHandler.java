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
public class ResultsCommandHandler extends ListenerAdapter {
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
        if (!"results".equals(event.getSubcommandGroup())) {
            return;
        }

        MessageCacheType type = switch (event.getSubcommandName()) {
            case "current" -> MessageCacheType.RESULTS_CURRENT;
            case "previous" -> MessageCacheType.RESULTS_PREVIOUS;
            case "standings" -> MessageCacheType.RESULTS_STANDINGS;
            case null, default -> null;
        };

        if (type == null) {
            return;
        }

        replyFromCache(event, type);
    }

    private void replyFromCache(SlashCommandInteractionEvent event, MessageCacheType type) {
        messageCache.getMessage(event.getChannelIdLong(), type).ifPresentOrElse(
                embed -> event.replyEmbeds(embed).queue(),
                () -> event.reply("Could not find any results for this request.").setEphemeral(true).queue()
        );
    }
}
