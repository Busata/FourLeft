ALTER TABLE gallery_photo
    ADD upload_time TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE gallery_photo
    ADD preview boolean default false;