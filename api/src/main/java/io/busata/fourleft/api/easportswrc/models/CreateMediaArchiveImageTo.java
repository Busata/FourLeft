package io.busata.fourleft.api.easportswrc.models;

import java.util.UUID;

public record CreateMediaArchiveImageTo(
        long attachmentId,
        UUID imageStoreUuid,
        Integer width,
        Integer height,
        String fileName,
        String contentType) {
}
