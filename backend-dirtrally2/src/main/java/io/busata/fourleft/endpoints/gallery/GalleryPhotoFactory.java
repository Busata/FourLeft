package io.busata.fourleft.endpoints.gallery;

import io.busata.fourleft.api.models.gallery.GalleryPhotoTo;
import io.busata.fourleft.api.models.gallery.GalleryPublicPhotoTo;
import io.busata.fourleft.api.models.gallery.GalleryTagNodeTo;
import io.busata.fourleft.api.models.gallery.GalleryTagOptionTo;
import io.busata.fourleft.domain.gallery.GalleryPhoto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class GalleryPhotoFactory {
    private final GalleryTagFactory galleryTagFactory;


    public GalleryPhotoTo create(GalleryPhoto photo) {
        return new GalleryPhotoTo(
                photo.getId(),
                photo.getTitle(),
                photo.isPublished(),
                photo.isPreview(),
                photo.getSelectedTags()
        );
    }

    public List<GalleryPublicPhotoTo> createPublic(Page<GalleryPhoto> photos) {
        List<GalleryTagNodeTo> allTags = galleryTagFactory.create();

        Map<String, String> tagLabels = allTags.stream().flatMap(tags -> tags.nodes().stream()).collect(Collectors.toMap(GalleryTagOptionTo::id, GalleryTagOptionTo::label));


        return photos.stream().map(photo -> {
            List<UUID> selectedTags = photo.getSelectedTags();

           List<String> tags = selectedTags.stream().map(selectedTag -> tagLabels.get(selectedTag.toString())).toList();

           return new GalleryPublicPhotoTo(
                    photo.getId(),
                    photo.getTitle(),
                   photo.isPreview(),
                   tags
            );
        }).toList();
    }
}
