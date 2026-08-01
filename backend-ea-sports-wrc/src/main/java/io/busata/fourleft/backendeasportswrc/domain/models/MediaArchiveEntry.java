package io.busata.fourleft.backendeasportswrc.domain.models;

import io.busata.fourleft.api.easportswrc.models.CreateMediaArchiveImageTo;
import io.busata.fourleft.api.easportswrc.models.CreateMediaArchivePostTo;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * One archived Discord image attachment. Rows sharing {@code messageId} form one feed post;
 * {@code attachmentId} (globally unique snowflake) dedupes reruns of the backfill.
 */
@Entity
@Getter
@NoArgsConstructor
public class MediaArchiveEntry {

    @Id
    private UUID id;

    private Long guildId;
    private long channelId;
    private String channelName;
    private long messageId;
    private long attachmentId;
    private Long authorId;
    private String authorUsername;
    private String authorDisplayName;
    private String authorAvatarUrl;
    private String caption;
    private Instant messageTimestamp;
    private UUID imageStoreUuid;
    private Integer width;
    private Integer height;
    private String fileName;
    private String contentType;
    private Instant createdAt;

    public MediaArchiveEntry(CreateMediaArchivePostTo post, CreateMediaArchiveImageTo image) {
        this.id = UUID.randomUUID();
        this.guildId = post.guildId();
        this.channelId = post.channelId();
        this.channelName = post.channelName();
        this.messageId = post.messageId();
        this.attachmentId = image.attachmentId();
        this.authorId = post.authorId();
        this.authorUsername = post.authorUsername();
        this.authorDisplayName = post.authorDisplayName();
        this.authorAvatarUrl = post.authorAvatarUrl();
        this.caption = post.caption();
        this.messageTimestamp = post.messageTimestamp();
        this.imageStoreUuid = image.imageStoreUuid();
        this.width = image.width();
        this.height = image.height();
        this.fileName = image.fileName();
        this.contentType = image.contentType();
        this.createdAt = Instant.now();
    }
}
