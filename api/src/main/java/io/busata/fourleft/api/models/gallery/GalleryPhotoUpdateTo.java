package io.busata.fourleft.api.models.gallery;

import java.util.List;
import java.util.UUID;

public record GalleryPhotoUpdateTo(
        String title,
         boolean published,
        List<UUID> tags
) {
}
