package io.busata.fourleft.application.gallery;

import io.busata.fourleft.domain.gallery.GalleryPhoto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GalleryPhotoRepository extends JpaRepository<GalleryPhoto, UUID> {


    List<GalleryPhoto> findByUserName(String username);


    @Query("SELECT distinct e FROM GalleryPhoto e FULL JOIN e.selectedTags t WHERE e.published=true and (upper(e.title) like upper(CONCAT('%',:query,'%'))) or t IN :tags")
    Page<GalleryPhoto> findPhotosByTagsOrTitle(Pageable page, @Param("query") String query, @Param("tags") List<UUID> tags);

    Page<GalleryPhoto> findGalleryPhotoByPublishedIsTrue(Pageable page);
}