package io.busata.fourleft.domain.gallery;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.sql.Array;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter

@NoArgsConstructor
public class GalleryPhoto {

    @Id
    UUID id;

    String userName;

    @Setter
    String title;

    @Setter
    boolean published;

    @Setter
    boolean preview;

    @ElementCollection
    List<UUID> selectedTags = new ArrayList<>();

    private ZonedDateTime uploadTime = ZonedDateTime.now();


    public GalleryPhoto(UUID id, String userName, boolean published, boolean preview) {
        this.id = id;
        this.userName = userName;
        this.published = published;
        this.preview = preview;
    }

    public void updateTags(List<UUID> tags) {
        this.selectedTags.clear();
        this.selectedTags.addAll(tags);
    }
}
