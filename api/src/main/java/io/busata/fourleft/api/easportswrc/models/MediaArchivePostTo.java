package io.busata.fourleft.api.easportswrc.models;

import java.time.Instant;
import java.util.List;

/** Feed post for the frontend; Discord snowflake ids are strings as they exceed JS safe integer range. */
public record MediaArchivePostTo(
        String messageId,
        String authorUsername,
        String authorDisplayName,
        String authorAvatarUrl,
        String caption,
        Instant timestamp,
        List<MediaArchiveImageTo> images) {
}
