package io.busata.fourleft.endpoints.gallery;

import io.busata.fourleft.api.RoutesTo;
import io.busata.fourleft.api.models.gallery.*;
import io.busata.fourleft.application.gallery.GalleryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequiredArgsConstructor
public class GalleryEndpoint {

    private final GalleryService galleryService;
    private final GalleryTagFactory galleryTagFactory;
    private final GalleryPhotoFactory galleryPhotoFactory;


    @PreAuthorize("permitAll()")
    @PostMapping(value= RoutesTo.GALLERY_UPLOAD)
    public GalleryPhotoTo uploadImage(@RequestParam("file") MultipartFile multipartFile) {
        return this.galleryPhotoFactory.create(galleryService.addPhoto(multipartFile));
    }

    @PreAuthorize("permitAll()")
    @DeleteMapping(value= RoutesTo.GALLER_USER_PHOTOS_BY_ID)
    public void deleteImage(@PathVariable UUID photoId) {
        galleryService.deletePhoto(photoId);
    }

    @PreAuthorize("permitAll()")
    @PutMapping(value= RoutesTo.GALLER_USER_PHOTOS_BY_ID)
    public GalleryPhotoTo updateImage(@PathVariable UUID photoId, @RequestBody GalleryPhotoUpdateTo update) {
        return galleryPhotoFactory.create(galleryService.updatePhoto(photoId, update.title(), update.published(), update.preview(), update.tags()));
    }

    @PreAuthorize("permitAll()")
    @GetMapping(value= RoutesTo.GALLER_USER_PHOTOS)
    public List<GalleryPhotoTo> getUserImages() {
        return galleryService.getUserImages().stream().map(galleryPhotoFactory::create).toList();
    }

    @GetMapping(value= RoutesTo.GALLERY_PUBLIC_PHOTOS)
    public List<GalleryPublicPhotoTo> getPublicImages(@RequestParam(value="query", required = false) String query, @RequestParam(value="page") Integer page) {
        if(StringUtils.isBlank(query)) {
            return galleryPhotoFactory.createPublic(galleryService.getAllPublishedImages(page));
        }

        var tokenized = Arrays.asList(query.split(","));

        List<GalleryTagNodeTo> allTags = galleryTagFactory.create();
        Map<String, String> tagLabels = allTags.stream()
                .flatMap(tags -> tags.nodes().stream().map(node -> Pair.of(node.id(), tags.label() + "\\" + node.label())))
                .collect(Collectors.toMap(Pair::getRight, Pair::getLeft));


        List<UUID> selectedTags = tagLabels.entrySet().stream()
                .filter((entry) -> {
                    boolean matchesTags = tokenized.stream().anyMatch(token -> {
                        boolean contains = entry.getKey().toLowerCase().contains(token.toLowerCase());
                        log.info("{} -> {}? {}", entry.getKey().toLowerCase(), token.toLowerCase(), contains);
                        return contains;
                    });

                    return matchesTags;
                })
                .map(Map.Entry::getValue)
                .map(UUID::fromString)
                .toList();



        return galleryPhotoFactory.createPublic(galleryService.getAllFilteredImages(page, query, selectedTags));
    }

    @PreAuthorize("permitAll()")
    @GetMapping(value = RoutesTo.GALLERY_TAG_GRAPH)
    public List<GalleryTagNodeTo> getTagGraph() {
       return galleryTagFactory.create();
    }

}
