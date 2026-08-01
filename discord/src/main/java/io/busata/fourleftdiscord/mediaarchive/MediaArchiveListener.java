package io.busata.fourleftdiscord.mediaarchive;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaArchiveListener extends ListenerAdapter {
    private final JDA client;
    private final MediaArchiveProperties properties;
    private final MediaArchiveService mediaArchiveService;

    @PostConstruct
    public void setupListener() {
        client.addEventListener(this);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromGuild() || !properties.isArchivedChannel(event.getChannel().getIdLong())) {
            return;
        }
        if (event.getAuthor().equals(event.getJDA().getSelfUser())) {
            return;
        }
        boolean hasImages = event.getMessage().getAttachments().stream().anyMatch(Message.Attachment::isImage);
        if (!hasImages) {
            return;
        }
        mediaArchiveService.submitArchive(event.getMessage());
    }
}
