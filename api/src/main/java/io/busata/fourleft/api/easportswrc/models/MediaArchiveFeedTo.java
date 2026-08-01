package io.busata.fourleft.api.easportswrc.models;

import java.util.List;

public record MediaArchiveFeedTo(
        List<MediaArchivePostTo> posts,
        String nextCursor,
        boolean hasMore) {
}
