package io.busata.fourleftdiscord.mediaarchive;

import io.busata.fourleftdiscord.eawrcsports.EAWRCBackendApi;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles {@code /fourleft archive backfill}: walks a channel's full message history and archives
 * every image attachment that is not already in the backend. Rerunnable — already-archived message
 * ids are skipped up front, and the backend dedupes on attachment id as a backstop.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MediaArchiveBackfillHandler extends ListenerAdapter {
    private static final int PROGRESS_INTERVAL = 50;

    private final JDA client;
    private final MediaArchiveService mediaArchiveService;
    private final EAWRCBackendApi backendApi;

    @PostConstruct
    public void setupListener() {
        client.addEventListener(this);
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("fourleft")) {
            return;
        }
        if (!"archive".equals(event.getSubcommandGroup()) || !"backfill".equals(event.getSubcommandName())) {
            return;
        }
        if (!event.isFromGuild()) {
            event.reply("This command only works in a server channel.").setEphemeral(true).queue();
            return;
        }

        GuildMessageChannel channel = resolveChannel(event);
        if (channel == null) {
            event.reply("That channel does not support message history.").setEphemeral(true).queue();
            return;
        }

        event.deferReply(true).queue();
        mediaArchiveService.submitBackfill(() -> runBackfill(event.getHook(), channel));
    }

    private GuildMessageChannel resolveChannel(SlashCommandInteractionEvent event) {
        try {
            var channelOption = event.getOption("channel");
            if (channelOption != null) {
                return channelOption.getAsChannel().asGuildMessageChannel();
            }
            return event.getChannel().asGuildMessageChannel();
        } catch (IllegalStateException ex) {
            return null;
        }
    }

    private void runBackfill(InteractionHook hook, GuildMessageChannel channel) {
        log.info("Starting media archive backfill for #{} ({})", channel.getName(), channel.getId());
        int archived = 0, skipped = 0, failed = 0;

        try {
            Set<Long> alreadyArchived = new HashSet<>(backendApi.getArchivedMediaMessageIds(channel.getIdLong()));

            for (Message message : channel.getIterableHistory()) {
                boolean hasImages = message.getAttachments().stream().anyMatch(Message.Attachment::isImage);
                if (!hasImages) {
                    continue;
                }
                if (alreadyArchived.contains(message.getIdLong())) {
                    skipped++;
                } else {
                    try {
                        if (mediaArchiveService.archiveMessage(message)) {
                            archived++;
                        } else {
                            skipped++;
                        }
                    } catch (Exception ex) {
                        failed++;
                        log.error("Backfill failed for message {} in #{}", message.getId(), channel.getName(), ex);
                    }
                }

                int processed = archived + skipped + failed;
                if (processed > 0 && processed % PROGRESS_INTERVAL == 0) {
                    report(hook, channel, "Backfill in progress: %d archived, %d skipped, %d failed…"
                            .formatted(archived, skipped, failed), false);
                }
            }

            report(hook, channel, "Backfill of #%s finished: %d archived, %d skipped, %d failed."
                    .formatted(channel.getName(), archived, skipped, failed), true);
            log.info("Media archive backfill for #{} finished: {} archived, {} skipped, {} failed",
                    channel.getName(), archived, skipped, failed);
        } catch (Exception ex) {
            log.error("Media archive backfill for #{} aborted", channel.getName(), ex);
            report(hook, channel, "Backfill of #%s aborted after %d archived: %s"
                    .formatted(channel.getName(), archived, ex.getMessage()), true);
        }
    }

    /** Interaction hooks expire after 15 minutes; long backfills fall back to a channel message. */
    private void report(InteractionHook hook, GuildMessageChannel channel, String content, boolean fallbackToChannel) {
        hook.editOriginal(content).queue(
                success -> {
                },
                error -> {
                    log.debug("Could not update backfill progress via interaction hook: {}", error.getMessage());
                    if (fallbackToChannel) {
                        channel.sendMessage(content).queue();
                    }
                });
    }
}
