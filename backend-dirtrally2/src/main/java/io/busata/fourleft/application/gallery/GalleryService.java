package io.busata.fourleft.application.gallery;

import io.busata.fourleft.domain.gallery.GalleryPhoto;
import io.busata.fourleft.infrastructure.clients.rendercache.RendercacheApi;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GalleryService {

    private final GalleryPhotoRepository photoRepository;
    private final RendercacheApi rendercacheApi;



    @Transactional
    public GalleryPhoto addPhoto(MultipartFile multipartFile) {
        UUID uuid = this.rendercacheApi.storeImage(multipartFile);
        String authenticatedUser = getAuthenticatedUser();

        return this.photoRepository.save(new GalleryPhoto(uuid, authenticatedUser, true));

    }

    private String getAuthenticatedUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .map(Principal::getName).orElseThrow();
    }

    public List<GalleryPhoto> getUserImages() {
        var user = getAuthenticatedUser();

        return this.photoRepository.findByUserName(user).stream().toList();
    }

    public Page<GalleryPhoto> getAllPublishedImages(Integer page) {
        Pageable pageRequest = PageRequest.of(page, 10);

        return this.photoRepository.findGalleryPhotoByPublishedIsTrue(pageRequest);
    }

    public Page<GalleryPhoto> getAllFilteredImages(Integer page, String query, List<UUID> selectedTags) {
        Pageable pageRequest = PageRequest.of(page, 10);

        return this.photoRepository.findPhotosByTagsOrTitle(pageRequest,query, selectedTags);
    }

    @Transactional
    public void deletePhoto(UUID photoId) {
        var user = getAuthenticatedUser();

        this.photoRepository.findById(photoId).ifPresentOrElse((galleryPhoto -> {
            if (Objects.equals(galleryPhoto.getUserName(), user)) {
                this.rendercacheApi.delete(photoId, "TODO");
                this.photoRepository.delete(galleryPhoto);
            }
        }), () -> {
            throw new RuntimeException("No permission to delete this picture.");
        });
    }

    public GalleryPhoto updatePhoto(UUID photoId, String title, boolean isPublished, List<UUID> uuids) {
        return this.photoRepository.findById(photoId).map((photo) -> {
            photo.setTitle(title);
            photo.setPublished(isPublished);
            photo.updateTags(uuids);

            return this.photoRepository.save(photo);
        }).orElseThrow();
    }
}
