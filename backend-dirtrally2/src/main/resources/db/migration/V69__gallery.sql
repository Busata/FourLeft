CREATE TABLE gallery_photo
(
    id        UUID    NOT NULL,
    user_name VARCHAR,
    title VARCHAR,
    published BOOLEAN NOT NULL,
    CONSTRAINT pk_galleryphoto PRIMARY KEY (id)
);

CREATE TABLE gallery_photo_selected_tags
(
    gallery_photo_id UUID NOT NULL,
    selected_tags    UUID
);

ALTER TABLE gallery_photo_selected_tags
    ADD CONSTRAINT fk_galleryphoto_selectedtags_on_gallery_photo FOREIGN KEY (gallery_photo_id) REFERENCES gallery_photo (id);