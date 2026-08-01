package io.busata.fourleft.backendeasportswrc.domain.services.mediaarchive;

import io.busata.fourleft.api.easportswrc.models.CreateMediaArchivePostTo;
import io.busata.fourleft.api.easportswrc.models.MediaArchiveFeedTo;
import io.busata.fourleft.api.easportswrc.models.MediaArchiveImageTo;
import io.busata.fourleft.api.easportswrc.models.MediaArchivePostTo;
import io.busata.fourleft.backendeasportswrc.domain.models.MediaArchiveEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaArchiveService {

    private static final int MAX_PAGE_SIZE = 50;

    private final MediaArchiveEntryRepository repository;

    /**
     * Saves each image as its own row/transaction so a rerun of the backfill (or the live listener
     * racing it) skips already-archived attachments instead of failing the whole post.
     */
    public void ingest(CreateMediaArchivePostTo post) {
        post.images().forEach(image -> {
            if (repository.existsByAttachmentId(image.attachmentId())) {
                return;
            }
            try {
                repository.save(new MediaArchiveEntry(post, image));
            } catch (DataIntegrityViolationException ex) {
                log.debug("Attachment {} already archived concurrently, skipping", image.attachmentId());
            }
        });
    }

    public List<Long> getArchivedMessageIds(long channelId) {
        return repository.findMessageIdsByChannelId(channelId);
    }

    /**
     * Keyset-cursor feed over distinct messages, newest first. Message ids are Discord snowflakes
     * (chronologically ordered), so the id doubles as the cursor and stays stable while new posts
     * arrive at the head.
     */
    public MediaArchiveFeedTo getFeed(Long before, int size) {
        int pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        PageRequest page = PageRequest.of(0, pageSize);

        List<Long> messageIds = before == null
                ? repository.findLatestMessageIds(page)
                : repository.findMessageIdsBefore(before, page);

        if (messageIds.isEmpty()) {
            return new MediaArchiveFeedTo(List.of(), null, false);
        }

        Map<Long, List<MediaArchiveEntry>> byMessage = repository.findByMessageIdInOrderByAttachmentIdAsc(messageIds)
                .stream()
                .collect(Collectors.groupingBy(MediaArchiveEntry::getMessageId));

        List<MediaArchivePostTo> posts = messageIds.stream()
                .map(byMessage::get)
                .map(this::toPost)
                .toList();

        String nextCursor = String.valueOf(messageIds.get(messageIds.size() - 1));
        return new MediaArchiveFeedTo(posts, nextCursor, messageIds.size() == pageSize);
    }

    private MediaArchivePostTo toPost(List<MediaArchiveEntry> entries) {
        MediaArchiveEntry first = entries.get(0);
        List<MediaArchiveImageTo> images = entries.stream()
                .sorted(Comparator.comparingLong(MediaArchiveEntry::getAttachmentId))
                .map(entry -> new MediaArchiveImageTo(entry.getImageStoreUuid().toString(), entry.getWidth(), entry.getHeight()))
                .toList();

        return new MediaArchivePostTo(
                String.valueOf(first.getMessageId()),
                first.getAuthorUsername(),
                first.getAuthorDisplayName(),
                first.getAuthorAvatarUrl(),
                first.getCaption(),
                first.getMessageTimestamp(),
                images);
    }
}
