package io.busata.fourleftdiscord.mediaarchive;

import feign.form.FormData;
import io.busata.fourleft.api.easportswrc.models.CreateMediaArchiveImageTo;
import io.busata.fourleft.api.easportswrc.models.CreateMediaArchivePostTo;
import io.busata.fourleftdiscord.eawrcsports.EAWRCBackendApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Archives the image attachments of a Discord message: bytes go to the external imagestore,
 * metadata goes to the WRC backend. All archive work (live messages and backfills) runs on a single
 * executor thread so JDA's event thread is never blocked and messages are processed one at a time.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MediaArchiveService {
    /** The imagestore rejects bigger uploads (multipart limit). */
    private static final long MAX_UPLOAD_BYTES = 20L * 1024 * 1024;

    private final ImageStoreApi imageStoreApi;
    private final EAWRCBackendApi backendApi;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void submitArchive(Message message) {
        executor.submit(() -> {
            try {
                archiveMessage(message);
            } catch (Exception ex) {
                log.error("Failed to archive message {}", message.getId(), ex);
            }
        });
    }

    public void submitBackfill(Runnable backfill) {
        executor.submit(backfill);
    }

    /**
     * @return true when at least one image was archived. Attachment downloads happen immediately —
     * Discord CDN urls are signed and expire, so they must never be stored for later.
     */
    public boolean archiveMessage(Message message) {
        List<Message.Attachment> images = message.getAttachments().stream()
                .filter(Message.Attachment::isImage)
                .toList();

        if (images.isEmpty() || message.getAuthor().equals(message.getJDA().getSelfUser())) {
            return false;
        }

        List<CreateMediaArchiveImageTo> stored = new ArrayList<>();
        for (Message.Attachment attachment : images) {
            if (attachment.getSize() > MAX_UPLOAD_BYTES) {
                log.warn("Skipping attachment {} of message {}: {} bytes exceeds imagestore limit",
                        attachment.getId(), message.getId(), attachment.getSize());
                continue;
            }
            stored.add(uploadToImageStore(attachment));
        }

        if (stored.isEmpty()) {
            return false;
        }

        backendApi.ingestMediaArchivePost(buildPost(message, stored));
        log.info("Archived {} image(s) from message {} in #{}", stored.size(), message.getId(), message.getChannel().getName());
        return true;
    }

    private CreateMediaArchiveImageTo uploadToImageStore(Message.Attachment attachment) {
        byte[] bytes;
        try (InputStream stream = attachment.getProxy().download().join()) {
            bytes = stream.readAllBytes();
        } catch (Exception ex) {
            throw new RuntimeException("Could not download attachment " + attachment.getId(), ex);
        }

        String contentType = Optional.ofNullable(attachment.getContentType()).orElse("application/octet-stream");
        UUID imageStoreUuid = imageStoreApi.storeImage(new FormData(contentType, attachment.getFileName(), bytes));

        return new CreateMediaArchiveImageTo(
                attachment.getIdLong(),
                imageStoreUuid,
                attachment.getWidth() > 0 ? attachment.getWidth() : null,
                attachment.getHeight() > 0 ? attachment.getHeight() : null,
                attachment.getFileName(),
                contentType);
    }

    private CreateMediaArchivePostTo buildPost(Message message, List<CreateMediaArchiveImageTo> images) {
        User author = message.getAuthor();
        // Member is null for history-fetched messages; the user-level effective name is the fallback.
        Member member = message.getMember();

        return new CreateMediaArchivePostTo(
                message.getIdLong(),
                message.getGuild().getIdLong(),
                message.getChannel().getIdLong(),
                message.getChannel().getName(),
                author.getIdLong(),
                author.getName(),
                member != null ? member.getEffectiveName() : author.getEffectiveName(),
                member != null ? member.getEffectiveAvatarUrl() : author.getEffectiveAvatarUrl(),
                message.getContentDisplay(),
                message.getTimeCreated().toInstant(),
                images);
    }
}
