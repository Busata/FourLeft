package io.busata.fourleft.api.easportswrc.models;

import java.time.Instant;
import java.util.List;

public record CreateMediaArchivePostTo(
        long messageId,
        long guildId,
        long channelId,
        String channelName,
        long authorId,
        String authorUsername,
        String authorDisplayName,
        String authorAvatarUrl,
        String caption,
        Instant messageTimestamp,
        List<CreateMediaArchiveImageTo> images) {
}
