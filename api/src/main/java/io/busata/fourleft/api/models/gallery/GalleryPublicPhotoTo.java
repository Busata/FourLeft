package io.busata.fourleft.api.models.gallery;

import java.util.List;
import java.util.UUID;

public record GalleryPublicPhotoTo(
        UUID id,
        String title,
        List<String> tags
) {
}
