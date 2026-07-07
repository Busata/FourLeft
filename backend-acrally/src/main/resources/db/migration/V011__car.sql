-- Reference catalogue of rally cars: name, model year, group and class. Admin-managed CRUD;
-- standalone (not linked to results). `group`/`class`/`year` are avoided as column names since
-- they are SQL keywords.
CREATE TABLE car
(
    id         UUID                        NOT NULL,
    name       VARCHAR(200)                NOT NULL,
    model_year INTEGER,
    group_name VARCHAR(120),
    class_name VARCHAR(120),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_car PRIMARY KEY (id)
);
-- Car names are unique case-insensitively.
CREATE UNIQUE INDEX ux_car_name ON car (LOWER(name));
