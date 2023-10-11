package io.busata.fourleft.api.models.gallery;

import java.util.List;
import java.util.UUID;

public record GalleryPhotoTo(
        UUID id,
        String title,
        boolean published,
        boolean preview,
        List<UUID> tags
) {
}
